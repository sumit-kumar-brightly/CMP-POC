package com.example.bluetoothdemo

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import platform.CoreBluetooth.*
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSNumber
import platform.Foundation.create
import platform.darwin.NSObject

class IosBtController(
    private val scope: CoroutineScope
) : BtController {

    private val _devices = MutableStateFlow<List<BtDevice>>(emptyList())
    private val devMap = mutableMapOf<String, BtDevice>()
    private val periphMap = mutableMapOf<String, CBPeripheral>()

    private val notifyFlows = mutableMapOf<Triple<String,String,String>, MutableSharedFlow<ByteArray>>()
    private val oneShotRead = mutableMapOf<Pair<String,String>, CompletableDeferred<ByteArray?>>()

    private var scanning = false

    private val delegate = CentralDelegate(this)
    private val cm = CBCentralManager(delegate, null)

    // waiters
    private val connectWaiters = mutableMapOf<String, CompletableDeferred<Boolean>>()
    private val servicesWaiter = mutableMapOf<String, CompletableDeferred<Boolean>>()              // by deviceId
    private val charsWaiter    = mutableMapOf<Pair<String,String>, CompletableDeferred<Boolean>>() // by (deviceId, serviceUUID)

    override suspend fun startScan() {
        scanning = true
        if (cm.state == CBManagerStatePoweredOn) cm.scanForPeripheralsWithServices(null, null)
    }
    override suspend fun stopScan() { scanning = false; cm.stopScan() }
    override fun devices(): Flow<List<BtDevice>> = _devices.asStateFlow()

    // ---------- Helpers ----------

    private fun periph(deviceId: String): CBPeripheral? = periphMap[deviceId]

    private fun isConnected(deviceId: String): Boolean {
        val p = periph(deviceId) ?: return false
        return p.state == CBPeripheralStateConnected
    }

    /** Ensure we are connected. If not, reconnect synchronously (with timeout). */
    private suspend fun ensureConnected(deviceId: String): Boolean {
        val p = periph(deviceId) ?: return false
        if (p.state == CBPeripheralStateConnected) return true

        p.setDelegate(delegate)
        val waiter = CompletableDeferred<Boolean>()
        connectWaiters[deviceId] = waiter
        cm.connectPeripheral(p, null)
        val ok = runCatching { withTimeout(8000) { waiter.await() } }.getOrDefault(false)
        connectWaiters.remove(deviceId)
        return ok
    }

    // ---------- Connect / Disconnect ----------

    override suspend fun connect(deviceId: String): Boolean {
        val p = periph(deviceId) ?: return false
        p.setDelegate(delegate)

        // stop scanning to avoid wearables dropping the link
        stopScan()

        val waiter = CompletableDeferred<Boolean>()
        connectWaiters[deviceId] = waiter
        cm.connectPeripheral(p, null)

        val ok = runCatching { withTimeout(8000) { waiter.await() } }.getOrDefault(false)
        connectWaiters.remove(deviceId)
        if (!ok) return false

        // Fully discover once so we have characteristics
        discoverServices(deviceId)

        // Resolve Device Name (0x1800/0x2A00)
        val ga = p.services?.firstOrNull { (it as CBService).UUID.UUIDString.equals("00001800-0000-1000-8000-00805F9B34FB", true) } as? CBService
        val nameChar = ga?.characteristics?.firstOrNull {
            (it as CBCharacteristic).UUID.UUIDString.equals("00002A00-0000-1000-8000-00805F9B34FB", true)
        } as? CBCharacteristic
        if (nameChar != null) {
            val cd = CompletableDeferred<ByteArray?>()
            oneShotRead[deviceId to nameChar.UUID.UUIDString] = cd
            p.readValueForCharacteristic(nameChar)
            val bytes = cd.await()
            val name = bytes?.decodeToString()?.takeIf { it.isNotBlank() }
            if (name != null) {
                devMap[deviceId] = (devMap[deviceId] ?: BtDevice(deviceId, null, true, false))
                    .copy(name = name, isConnected = true)
                _devices.value = devMap.values.sortedBy { it.name ?: it.id }
            }
        }
        return true
    }

    override suspend fun disconnect(deviceId: String) {
        periph(deviceId)?.let { cm.cancelPeripheralConnection(it) }
        devMap[deviceId]?.let { devMap[deviceId] = it.copy(isConnected = false) }
        _devices.value = devMap.values.sortedBy { it.name ?: it.id }
    }

    // ---------- Services / Characteristics ----------

    override suspend fun discoverServices(deviceId: String): List<GattServiceInfo> {
        val p = periph(deviceId) ?: return emptyList()
        if (!ensureConnected(deviceId)) return emptyList()

        // 1) Discover services and wait
        val svcWaiter = CompletableDeferred<Boolean>()
        servicesWaiter[deviceId] = svcWaiter
        p.discoverServices(null)
        val servicesOk = runCatching { withTimeout(8000) { svcWaiter.await() } }.getOrDefault(false)
        servicesWaiter.remove(deviceId)
        if (!servicesOk) return emptyList()

        // 2) For each service, discover characteristics and wait
        val services = p.services?.map { it as CBService } ?: emptyList()
        for (svc in services) {
            val key = deviceId to svc.UUID.UUIDString
            val chWait = CompletableDeferred<Boolean>()
            charsWaiter[key] = chWait
            p.discoverCharacteristics(null, svc)
            runCatching { withTimeout(8000) { chWait.await() } }
            charsWaiter.remove(key)
        }

        // 3) Build result with characteristics populated
        val out = p.services?.map { sAny ->
            val s = sAny as CBService
            val chars = s.characteristics?.map { cAny ->
                val c = cAny as CBCharacteristic
                val props = mutableSetOf<CharProp>()
                val pm = c.properties.toInt()
                if (pm and CBCharacteristicPropertyRead.toInt() != 0) props += CharProp.READ
                if (pm and CBCharacteristicPropertyWrite.toInt() != 0) props += CharProp.WRITE
                if (pm and CBCharacteristicPropertyWriteWithoutResponse.toInt() != 0) props += CharProp.WRITE_NO_RSP
                if (pm and CBCharacteristicPropertyNotify.toInt() != 0) props += CharProp.NOTIFY
                if (pm and CBCharacteristicPropertyIndicate.toInt() != 0) props += CharProp.INDICATE
                GattCharInfo(c.UUID.UUIDString, props)
            } ?: emptyList()
            GattServiceInfo(s.UUID.UUIDString, chars)
        } ?: emptyList()

        return out
    }

    override suspend fun readCharacteristic(deviceId: String, serviceUuid: String, charUuid: String): ByteArray? {
        val p = periph(deviceId) ?: return null
        if (!ensureConnected(deviceId)) return null
        val s = p.services?.firstOrNull { (it as CBService).UUID.UUIDString.equals(serviceUuid, true) } as? CBService ?: return null
        val c = s.characteristics?.firstOrNull { (it as CBCharacteristic).UUID.UUIDString.equals(charUuid, true) } as? CBCharacteristic ?: return null
        val cd = CompletableDeferred<ByteArray?>()
        oneShotRead[deviceId to charUuid] = cd
        p.readValueForCharacteristic(c)
        return cd.await()
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun writeCharacteristic(
        deviceId: String,
        serviceUuid: String,
        charUuid: String,
        value: ByteArray,
        withResponse: Boolean
    ): Boolean {
        val p = periph(deviceId) ?: return false
        if (!ensureConnected(deviceId)) return false
        val s = p.services?.firstOrNull { (it as CBService).UUID.UUIDString.equals(serviceUuid, true) } as? CBService ?: return false
        val c = s.characteristics?.firstOrNull { (it as CBCharacteristic).UUID.UUIDString.equals(charUuid, true) } as? CBCharacteristic ?: return false

        val data: NSData = value.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = value.size.toULong())
        }
        p.writeValue(data, c, if (withResponse) CBCharacteristicWriteWithResponse else CBCharacteristicWriteWithoutResponse)
        return true
    }

    override suspend fun enableNotifications(deviceId: String, serviceUuid: String, charUuid: String, enable: Boolean): Boolean {
        val p = periph(deviceId) ?: return false
        if (!ensureConnected(deviceId)) return false
        val s = p.services?.firstOrNull { (it as CBService).UUID.UUIDString.equals(serviceUuid, true) } as? CBService ?: return false
        val c = s.characteristics?.firstOrNull { (it as CBCharacteristic).UUID.UUIDString.equals(charUuid, true) } as? CBCharacteristic ?: return false
        p.setNotifyValue(enable, c)
        return true
    }

    override fun notifications(deviceId: String, serviceUuid: String, charUuid: String): Flow<ByteArray> {
        val key = Triple(deviceId, serviceUuid, charUuid)
        return notifyFlows.getOrPut(key) { MutableSharedFlow(extraBufferCapacity = 64) }.asSharedFlow()
    }

    // 🔹 UPDATED: per-device battery update
    override suspend fun readBatteryPercent(deviceId: String): Int? {
        // Ensure we are connected & discovered
        val svcs = discoverServices(deviceId)
        val svc = svcs.firstOrNull {
            it.uuid.equals("0000180F-0000-1000-8000-00805F9B34FB", true) ||
                    it.uuid.equals("180F", true) || it.uuid.endsWith("180f", true)
        } ?: return null
        val chr = svc.characteristics.firstOrNull {
            it.uuid.equals("00002A19-0000-1000-8000-00805F9B34FB", true) ||
                    it.uuid.equals("2A19", true) || it.uuid.endsWith("2a19", true)
        } ?: return null
        val bytes = readCharacteristic(deviceId, svc.uuid, chr.uuid) ?: return null
        val v = bytes.firstOrNull()?.toInt()?.and(0xFF)

        // ✅ Update only this device’s battery and emit
        val cur = devMap[deviceId] ?: BtDevice(deviceId, null, isBle = true, isClassic = false)
        devMap[deviceId] = cur.copy(batteryPercent = v)
        _devices.value = devMap.values.sortedBy { it.name ?: it.id }

        return v
    }

    // ---------- Classic stubs ----------
    override suspend fun classicSend(deviceId: String, payload: ByteArray): Boolean = false
    override fun classicIncoming(deviceId: String): Flow<ByteArray> = emptyFlow()

    // ---------- Callbacks from delegate ----------
    internal fun onManagerStatePoweredOn() {
        if (scanning) cm.scanForPeripheralsWithServices(null, null)
    }

    internal fun onDiscoverPeripheral(peripheral: CBPeripheral, rssi: NSNumber) {
        val id = peripheral.identifier.UUIDString
        periphMap[id] = peripheral
        val entry = (devMap[id] ?: BtDevice(id, null, true, false))
        devMap[id] = entry.copy(name = peripheral.name, rssi = rssi.intValue)
        _devices.value = devMap.values.sortedBy { it.name ?: it.id }
    }

    internal fun onDidConnect(peripheral: CBPeripheral) {
        val id = peripheral.identifier.UUIDString
        devMap[id]?.let { devMap[id] = it.copy(isConnected = true) }
        _devices.value = devMap.values.sortedBy { it.name ?: it.id }
        connectWaiters[id]?.complete(true)
    }

    internal fun onDidFailToConnect(peripheral: CBPeripheral) {
        connectWaiters[peripheral.identifier.UUIDString]?.complete(false)
    }

    internal fun onDidDisconnect(peripheral: CBPeripheral) {
        val id = peripheral.identifier.UUIDString
        devMap[id]?.let { devMap[id] = it.copy(isConnected = false) }
        _devices.value = devMap.values.sortedBy { it.name ?: it.id }
    }

    internal fun onCharacteristicValueChanged(peripheral: CBPeripheral, characteristic: CBCharacteristic) {
        val id = peripheral.identifier.UUIDString
        val cu = characteristic.UUID.UUIDString
        val svcUuid = characteristic.service?.UUID?.UUIDString ?: return
        val bytes = characteristic.value?.toByteArray()

        oneShotRead.remove(id to cu)?.complete(bytes)
        val key = Triple(id, svcUuid, cu)
        notifyFlows[key]?.tryEmit(bytes ?: ByteArray(0))
    }

    internal fun onDidDiscoverServices(peripheral: CBPeripheral) {
        servicesWaiter[peripheral.identifier.UUIDString]?.complete(true)
    }

    internal fun onDidDiscoverCharacteristics(peripheral: CBPeripheral, service: CBService) {
        val key = peripheral.identifier.UUIDString to service.UUID.UUIDString
        charsWaiter[key]?.complete(true)
    }
}

// Objective-C side (isolated)
private class CentralDelegate(
    private val owner: IosBtController
) : NSObject(), CBCentralManagerDelegateProtocol, CBPeripheralDelegateProtocol {

    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        if (central.state == CBManagerStatePoweredOn) owner.onManagerStatePoweredOn()
    }

    @ObjCSignatureOverride
    override fun centralManager(
        central: CBCentralManager,
        didDiscoverPeripheral: CBPeripheral,
        advertisementData: Map<Any?, *>,
        RSSI: NSNumber
    ) {
        owner.onDiscoverPeripheral(didDiscoverPeripheral, RSSI)
    }

    @ObjCSignatureOverride
    override fun centralManager(central: CBCentralManager, didConnectPeripheral: CBPeripheral) {
        owner.onDidConnect(didConnectPeripheral)
    }

    @ObjCSignatureOverride
    override fun centralManager(
        central: CBCentralManager,
        didFailToConnectPeripheral: CBPeripheral,
        error: NSError?
    ) {
        owner.onDidFailToConnect(didFailToConnectPeripheral)
    }

    @ObjCSignatureOverride
    override fun centralManager(
        central: CBCentralManager,
        didDisconnectPeripheral: CBPeripheral,
        error: NSError?
    ) {
        owner.onDidDisconnect(didDisconnectPeripheral)
    }

    // Peripheral delegate

    @ObjCSignatureOverride
    override fun peripheral(peripheral: CBPeripheral, didDiscoverServices: NSError?) {
        owner.onDidDiscoverServices(peripheral)
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didDiscoverCharacteristicsForService: CBService,
        error: NSError?
    ) {
        owner.onDidDiscoverCharacteristics(peripheral, didDiscoverCharacteristicsForService)
    }

    @ObjCSignatureOverride
    override fun peripheral(
        peripheral: CBPeripheral,
        didUpdateValueForCharacteristic: CBCharacteristic,
        error: NSError?
    ) {
        owner.onCharacteristicValueChanged(peripheral, didUpdateValueForCharacteristic)
    }
}