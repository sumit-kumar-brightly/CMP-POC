package com.example.bluetoothdemo

data class BtDevice(
    val id: String,               // Android: MAC; iOS: UUID string
    val name: String?,
    val isBle: Boolean,
    val isClassic: Boolean,
    val rssi: Int? = null,
    val isConnected: Boolean = false,
    val isBonded: Boolean = false,
    val isAudioVideo: Boolean = false,
    val batteryPercent: Int? = null
)

data class GattServiceInfo(
    val uuid: String,
    val characteristics: List<GattCharInfo>
)

data class GattCharInfo(
    val uuid: String,
    val properties: Set<CharProp>
)

enum class CharProp { READ, WRITE, WRITE_NO_RSP, NOTIFY, INDICATE }