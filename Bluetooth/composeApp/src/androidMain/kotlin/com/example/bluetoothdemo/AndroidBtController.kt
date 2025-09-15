package com.example.bluetoothdemo

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.LinkedHashMap

// ---- SIG UUIDs (standard) via helper (avoids long 128-bit literals)
private fun sigUuid16(hex: String): UUID =
    UUID.fromString("0000${hex.lowercase()}-0000-1000-8000-00805f9b34fb")

private val UUID_GAP_SERVICE = sigUuid16("1800")
private val UUID_DEVICE_NAME = sigUuid16("2A00")
private val UUID_DIS         = sigUuid16("180A")
private val UUID_MFR_NAME    = sigUuid16("2A29")
private val UUID_MODEL_NUM   = sigUuid16("2A24")
private val UUID_FIRMWARE    = sigUuid16("2A26")
private val UUID_HARDWARE    = sigUuid16("2A27")
private val UUID_SOFTWARE    = sigUuid16("2A28")
private val UUID_SERIAL      = sigUuid16("2A25")
private val UUID_BATTERY_SVC = sigUuid16("180F")
private val UUID_BATTERY_LVL = sigUuid16("2A19")

class AndroidBtController(
    private val context: Context,
    private val scope: CoroutineScope
) : BtController {

    private val manager = context.getSystemService(BluetoothManager::class.java)
    private val adapter = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? get() = adapter?.bluetoothLeScanner

    // Devices (BLE only)
    private val bleDevices = LinkedHashMap<String, BtDevice>()
    private val _devices   = MutableSharedFlow<List<BtDevice>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override fun devices(): Flow<List<BtDevice>> = _devices.asSharedFlow()
    private fun emit() = scope.launch(Dispatchers.Main) { _devices.emit(bleDevices.values.sortedBy { it.name ?: it.id }) }

    // Permissions
    private fun hasScan(): Boolean =
        if (Build.VERSION.SDK_INT >= 31)
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        else
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun hasConnect(): Boolean =
        if (Build.VERSION.SDK_INT >= 31)
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        else true

    // Aggressive scan settings
    private val scanSettings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
        .build()

    // Scan (BLE only)
    private var scanning = false

    @SuppressLint("MissingPermission")
    override suspend fun startScan() {
        if (scanning) return
        scanning = true
        if (adapter == null || !adapter.isEnabled) { scanning = false; return }
        if (hasScan()) {
            runCatching { scanner?.stopScan(bleCb) } // clean start
            scanner?.startScan(null, scanSettings, bleCb)
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun stopScan() {
        if (!scanning) return
        scanning = false
        runCatching { scanner?.stopScan(bleCb) }
    }

    private val bleCb = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(type: Int, result: ScanResult) {
            // Only require SCAN to list devices (do not require CONNECT here)
            if (!hasScan()) return
            val dev = result.device ?: return
            val id = dev.address ?: return
            bleDevices[id] = BtDevice(
                id = id,
                name = dev.name,
                isBle = true,
                isClassic = false,
                rssi = result.rssi,
                isConnected = isConnectedGatt(id) // internally checks CONNECT
            )
            emit()
        }

        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            if (!hasScan()) return
            results.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
        }
    }

    @SuppressLint("MissingPermission")
    private fun isConnectedGatt(id: String): Boolean {
        if (!hasConnect()) return false
        return manager?.getConnectedDevices(BluetoothProfile.GATT).orEmpty().any { it.address == id }
    }

    // GATT state / waiters
    private val gattMap       = HashMap<String, BluetoothGatt>()
    private val gattCallbacks = HashMap<String, BluetoothGattCallback>()
    private val notifyFlows   = mutableMapOf<Triple<String,String,String>, MutableSharedFlow<ByteArray>>()
    private data class ReadRes(val bytes: ByteArray?, val status: Int) // <── keep SINGLE definition
    private val readWaiters   = HashMap<Triple<String,String,String>, CompletableDeferred<ReadRes>>()
    private val svcWaiters    = HashMap<String, CompletableDeferred<Boolean>>()
    private val connWaiters   = HashMap<String, CompletableDeferred<Boolean>>()
    private val mtuWaiters    = HashMap<String, CompletableDeferred<Boolean>>()
    private val bondWaiters   = HashMap<String, CompletableDeferred<Boolean>>()

    // Connect / Disconnect (BLE only)
    @SuppressLint("MissingPermission")
    override suspend fun connect(deviceId: String): Boolean = withContext(Dispatchers.IO) {
        val remote = adapter?.getRemoteDevice(deviceId) ?: return@withContext false
        stopScan()

        val connected = CompletableDeferred<Boolean>()
        connWaiters[deviceId] = connected

        val cb = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gattMap[deviceId] = g
                    bleDevices[deviceId] = (bleDevices[deviceId] ?: BtDevice(deviceId, null, true, false)).copy(isConnected = true)
                    emit()
                    connWaiters.remove(deviceId)?.complete(true)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    bleDevices[deviceId]?.let { bleDevices[deviceId] = it.copy(isConnected = false) }
                    emit()
                    connWaiters.remove(deviceId)?.complete(false)
                }
            }
            override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
                mtuWaiters.remove(deviceId)?.complete(status == BluetoothGatt.GATT_SUCCESS)
            }
            override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
                svcWaiters.remove(deviceId)?.complete(status == BluetoothGatt.GATT_SUCCESS)
            }
            override fun onCharacteristicRead(g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int) {
                val key = Triple(deviceId, (ch.service?.uuid?.toString() ?: "").lowercase(), ch.uuid.toString().lowercase())
                val b = if (status == BluetoothGatt.GATT_SUCCESS) ch.value else null
                readWaiters.remove(key)?.complete(ReadRes(b, status))
            }
            override fun onCharacteristicChanged(g: BluetoothGatt, ch: BluetoothGattCharacteristic) {
                val key = Triple(deviceId, ch.service?.uuid?.toString() ?: return, ch.uuid.toString())
                notifyFlows[key]?.tryEmit(ch.value ?: ByteArray(0))
            }
        }
        gattCallbacks[deviceId] = cb

        val gatt = remote.connectGatt(context, false, cb, BluetoothDevice.TRANSPORT_LE)
        val ok = runCatching { withTimeout(8000) { connected.await() } }.getOrDefault(false)
        if (!ok) { runCatching { gatt.close() }; return@withContext false }

        // Proactive bond (DIS often requires encryption)
        ensureBond(remote)

        // MTU then first discovery
        val mtuW = CompletableDeferred<Boolean>()
        mtuWaiters[deviceId] = mtuW
        if (gatt.requestMtu(517)) runCatching { withTimeout(4000) { mtuW.await() } }
        mtuWaiters.remove(deviceId)

        discoverServices(deviceId)

        // Resolve device name
        runCatching {
            readCharacteristic(deviceId, UUID_GAP_SERVICE.toString(), UUID_DEVICE_NAME.toString())
                ?.decodeString()?.takeIf { it.isNotBlank() }?.let { nm ->
                    bleDevices[deviceId] = (bleDevices[deviceId] ?: BtDevice(deviceId, null, true, false)).copy(name = nm)
                    emit()
                }
        }
        true
    }

    @SuppressLint("MissingPermission")
    override suspend fun disconnect(deviceId: String) {
        gattMap.remove(deviceId)?.let { runCatching { it.disconnect(); it.close() } }
        gattCallbacks.remove(deviceId)
        bleDevices[deviceId]?.let { bleDevices[deviceId] = it.copy(isConnected = false) }
        emit()
    }

    // Discover (flatten included services)
    @SuppressLint("MissingPermission")
    override suspend fun discoverServices(deviceId: String): List<GattServiceInfo> = withContext(Dispatchers.IO) {
        val gatt = gattMap[deviceId] ?: return@withContext emptyList()
        suspend fun doDiscover(): List<BluetoothGattService> {
            val w = CompletableDeferred<Boolean>()
            svcWaiters[deviceId] = w
            gatt.discoverServices()
            val ok = runCatching { withTimeout(8000) { w.await() } }.getOrDefault(false)
            svcWaiters.remove(deviceId)
            return if (ok) gatt.services.orEmpty() else emptyList()
        }
        val root = doDiscover()
        val all = LinkedHashMap<UUID, BluetoothGattService>()
        fun collect(s: BluetoothGattService) {
            if (all.putIfAbsent(s.uuid, s) != null) return
            s.includedServices?.forEach { collect(it) }
        }
        root.forEach { collect(it) }
        all.values.map { s ->
            val chars = s.characteristics.orEmpty().map { c ->
                val props = mutableSetOf<CharProp>()
                val p = c.properties
                if (p and BluetoothGattCharacteristic.PROPERTY_READ != 0) props += CharProp.READ
                if (p and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) props += CharProp.WRITE
                if (p and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) props += CharProp.WRITE_NO_RSP
                if (p and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) props += CharProp.NOTIFY
                if (p and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) props += CharProp.INDICATE
                GattCharInfo(c.uuid.toString(), props)
            }
            GattServiceInfo(s.uuid.toString(), chars)
        }
    }

    // Find a characteristic anywhere (primary + included)
    private fun findCharAnywhere(gatt: BluetoothGatt, charUuid: UUID): BluetoothGattCharacteristic? {
        val q: ArrayDeque<BluetoothGattService> = ArrayDeque()
        gatt.services?.forEach { q.add(it) }
        val seen = HashSet<UUID>()
        while (q.isNotEmpty()) {
            val s = q.removeFirst()
            if (!seen.add(s.uuid)) continue
            s.characteristics?.firstOrNull { it.uuid == charUuid }?.let { return it }
            s.includedServices?.forEach { q.add(it) }
        }
        return null
    }

    // Read / Write / Notify
    @SuppressLint("MissingPermission")
    override suspend fun readCharacteristic(deviceId: String, serviceUuid: String, charUuid: String): ByteArray? =
        withContext(Dispatchers.IO) {
            val gatt = gattMap[deviceId] ?: return@withContext null
            if (gatt.services.isNullOrEmpty()) runCatching { discoverServices(deviceId) }

            val target = findCharAnywhere(gatt, UUID.fromString(charUuid)) ?: return@withContext null

            suspend fun doReadOnce(): ReadRes? {
                val key = Triple(deviceId, (target.service?.uuid?.toString() ?: "").lowercase(), target.uuid.toString().lowercase())
                val waiter = CompletableDeferred<ReadRes>()
                synchronized(readWaiters) { readWaiters[key] = waiter }
                if (!gatt.readCharacteristic(target)) {
                    synchronized(readWaiters) { readWaiters.remove(key) }
                    return null
                }
                val res = runCatching { withTimeout(8000) { waiter.await() } }.getOrNull()
                synchronized(readWaiters) { readWaiters.remove(key) }
                return res
            }

            val first = doReadOnce()

            val needSec = first?.status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION ||
                    first?.status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION
            if (needSec) {
                val remote = adapter?.getRemoteDevice(deviceId)
                if (remote != null && ensureBond(remote)) {
                    delay(250)
                    val second = doReadOnce()
                    return@withContext second?.bytes
                }
            }

            if ((first?.bytes == null || first.bytes.isEmpty())
                && (target.service?.uuid == UUID_DIS || target.service?.uuid.toString().endsWith("180a", true))) {
                val remote = adapter?.getRemoteDevice(deviceId)
                if (remote != null && remote.bondState != BluetoothDevice.BOND_BONDED && ensureBond(remote)) {
                    delay(250)
                    val again = doReadOnce()
                    return@withContext again?.bytes
                }
            }

            first?.bytes
        }

    @SuppressLint("MissingPermission")
    override suspend fun writeCharacteristic(
        deviceId: String,
        serviceUuid: String,
        charUuid: String,
        value: ByteArray,
        withResponse: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val gatt = gattMap[deviceId] ?: return@withContext false
        val c = findCharAnywhere(gatt, UUID.fromString(charUuid)) ?: return@withContext false
        c.writeType = if (withResponse) BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        else BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        c.value = value
        gatt.writeCharacteristic(c)
    }

    @SuppressLint("MissingPermission")
    override suspend fun enableNotifications(deviceId: String, serviceUuid: String, charUuid: String, enable: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            val gatt = gattMap[deviceId] ?: return@withContext false
            val c = findCharAnywhere(gatt, UUID.fromString(charUuid)) ?: return@withContext false
            val okSet = gatt.setCharacteristicNotification(c, enable)
            val cccd = c.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"))
            if (cccd != null) {
                cccd.value = if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(cccd)
            }
            okSet
        }

    override fun notifications(deviceId: String, serviceUuid: String, charUuid: String): Flow<ByteArray> {
        val key = Triple(deviceId, serviceUuid, charUuid)
        return notifyFlows.getOrPut(key) {
            MutableSharedFlow(replay = 0, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        }.asSharedFlow()
    }

    // Battery
    @SuppressLint("MissingPermission")
    override suspend fun readBatteryPercent(deviceId: String): Int? = withContext(Dispatchers.IO) {
        val b = readCharacteristic(deviceId, UUID_BATTERY_SVC.toString(), UUID_BATTERY_LVL.toString()) ?: return@withContext null
        val v = b.firstOrNull()?.toInt()?.and(0xFF)
        bleDevices[deviceId]?.let { bleDevices[deviceId] = it.copy(batteryPercent = v) }
        emit()
        v
    }

    // Classic stubs for interface
    override suspend fun classicSend(deviceId: String, payload: ByteArray): Boolean = false
    override fun classicIncoming(deviceId: String): Flow<ByteArray> = emptyFlow()

    // Bond helper
    @SuppressLint("MissingPermission")
    private suspend fun ensureBond(remote: BluetoothDevice): Boolean = withContext(Dispatchers.IO) {
        if (remote.bondState == BluetoothDevice.BOND_BONDED) return@withContext true
        val waiter = CompletableDeferred<Boolean>()
        bondWaiters[remote.address] = waiter
        val rcv = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: android.content.Intent?) {
                if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val dev: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (dev?.address == remote.address) {
                        when (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)) {
                            BluetoothDevice.BOND_BONDED -> bondWaiters.remove(remote.address)?.complete(true)
                            BluetoothDevice.BOND_NONE   -> bondWaiters.remove(remote.address)?.complete(false)
                        }
                    }
                }
            }
        }
        context.registerReceiver(rcv, android.content.IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
        try {
            if (remote.createBond()) withTimeout(15000) { waiter.await() } else false
        } catch (_: Throwable) { false }
        finally { runCatching { context.unregisterReceiver(rcv) }; bondWaiters.remove(remote.address) }
    }
}

// String decoder (trim NUL & fallback charsets)
private fun ByteArray.decodeString(): String {
    fun ByteArray.trimNulls() = if (isNotEmpty() && last() == 0.toByte()) dropLast(1).toByteArray() else this
    val a = trimNulls()
    return try { a.toString(Charsets.UTF_8) } catch (_: Throwable) {
        try { a.toString(Charset.forName("US-ASCII")) } catch (_: Throwable) {
            try { a.toString(Charset.forName("ISO-8859-1")) } catch (_: Throwable) { "" }
        }
    }.trim()
}