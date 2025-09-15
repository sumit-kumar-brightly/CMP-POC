package com.example.bluetoothdemo

import kotlinx.coroutines.flow.Flow

interface BtController {
    suspend fun startScan()
    suspend fun stopScan()
    fun devices(): Flow<List<BtDevice>>

    suspend fun connect(deviceId: String): Boolean
    suspend fun disconnect(deviceId: String)

    suspend fun discoverServices(deviceId: String): List<GattServiceInfo>
    suspend fun readCharacteristic(deviceId: String, serviceUuid: String, charUuid: String): ByteArray?
    suspend fun writeCharacteristic(deviceId: String, serviceUuid: String, charUuid: String, value: ByteArray, withResponse: Boolean = true): Boolean
    fun notifications(deviceId: String, serviceUuid: String, charUuid: String): Flow<ByteArray>
    suspend fun enableNotifications(deviceId: String, serviceUuid: String, charUuid: String, enable: Boolean): Boolean

    // Android Classic SPP (iOS will no-op)
    suspend fun classicSend(deviceId: String, payload: ByteArray): Boolean
    fun classicIncoming(deviceId: String): Flow<ByteArray>

    suspend fun readBatteryPercent(deviceId: String): Int?
}