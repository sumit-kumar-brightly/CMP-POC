
package com.example.bluetoothdemo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(controller: BtController) {
    val scope = rememberCoroutineScope()
    val devices by controller.devices().collectAsState(initial = emptyList())

    var connectingId by remember { mutableStateOf<String?>(null) }
    var dialogDeviceId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("CMP Bluetooth Demo") }) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            // Scan controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { scope.launch { controller.startScan() } }) { Text("Scan") }
                OutlinedButton(onClick = { scope.launch { controller.stopScan() } }) { Text("Stop") }
                Spacer(Modifier.weight(1f))
                Text("Found: ${devices.size}")
            }

            Spacer(Modifier.height(8.dp))

            if (devices.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No devices yet. Tap Scan.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(devices, key = { it.id }) { d ->
                        DeviceRow(
                            device = d,
                            isConnecting = (connectingId == d.id),
                            onConnectClick = {
                                connectingId = d.id
                                scope.launch {
                                    println("[UI] Connecting to ${d.id} ...")
                                    val ok = controller.connect(d.id)
                                    connectingId = null
                                    if (ok) {
                                        println("[UI] Connected ${d.id}")
                                        dialogDeviceId = d.id
                                    } else {
                                        println("[UI] Connect failed ${d.id}")
                                    }
                                }
                            },
                            onOpenDetails = { dialogDeviceId = d.id }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    // Device details dialog
    if (dialogDeviceId != null) {
        DeviceDetailDialog(
            controller = controller,
            deviceId = dialogDeviceId!!,
            onClose = { dialogDeviceId = null }
        )
    }
}

/* ---------------- UI pieces ---------------- */

@Composable
private fun DeviceRow(
    device: BtDevice,
    isConnecting: Boolean,
    onConnectClick: () -> Unit,
    onOpenDetails: () -> Unit
) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        headlineContent = {
            Text(
                text = device.name ?: "(unknown)",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = device.id,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "BLE:${device.isBle}  Classic:${device.isClassic}  |  RSSI:${device.rssi ?: "-"}  |  Battery:${device.batteryPercent ?: "-"}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (device.isConnected) {
                    AssistChip(onClick = onOpenDetails, label = { Text("Details") })
                }
                Button(onClick = onConnectClick, enabled = !isConnecting) {
                    Text(if (isConnecting) "Connecting…" else "Connect")
                }
            }
        }
    )
}

@Composable
private fun DeviceDetailDialog(
    controller: BtController,
    deviceId: String,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val devices by controller.devices().collectAsState(initial = emptyList())
    val device = devices.firstOrNull { it.id == deviceId }

    var services by remember { mutableStateOf<List<GattServiceInfo>>(emptyList()) }
    var lastRead by remember { mutableStateOf<ByteArray?>(null) }
    var parsedText by remember { mutableStateOf<String?>(null) }

    // Form state
    var showForm by remember { mutableStateOf(false) }
    var formRows by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var formBusy by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onClose) {
        Surface(
            tonalElevation = 8.dp,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxSize(0.95f)
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                /* ---- Header ---- */
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            device?.name ?: "(unknown)",
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            device?.id ?: deviceId,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Connected: ${device?.isConnected ?: false}  |  Battery: ${device?.batteryPercent ?: "-"}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(onClick = onClose) { Text("Close") }
                }

                Spacer(Modifier.height(10.dp))

                /* ---- Actions ---- */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = {
                        scope.launch { controller.readBatteryPercent(deviceId) }
                    }) { Text("Battery") }

                    OutlinedButton(onClick = {
                        scope.launch { services = controller.discoverServices(deviceId) }
                    }) { Text("Discover") }

                    // NEW: Show Details Form
                    OutlinedButton(onClick = {
                        scope.launch {
                            formBusy = true
                            try {
                                if (services.isEmpty()) {
                                    services = controller.discoverServices(deviceId)
                                }
                                formRows = readDetailsForm(controller, deviceId, services)
                                showForm = true
                            } finally {
                                formBusy = false
                            }
                        }
                    }) { Text(if (formBusy) "Loading…" else "Show Details Form") }
                }

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(8.dp))

                parsedText?.let { Text(it) }
                lastRead?.let { Text("Last Read: ${it.toPrettyHex()}  |  \"${it.toUtf8Safe()}\"") }

                Spacer(Modifier.height(10.dp))

                Text("GATT Services (${services.size})", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(services) { svc ->
                        Card {
                            Column(Modifier.padding(10.dp)) {
                                Text("Service: ${svc.uuid}", style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(4.dp))
                                svc.characteristics.forEach { ch ->
                                    Column {
                                        val shortName = gattNames[uuidShort(ch.uuid)] ?: uuidShort(ch.uuid)
                                        Text("  Char: $shortName (${ch.uuid})  props=${ch.properties}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        if (ch.properties.contains(CharProp.READ)) {
                                            Button(onClick = {
                                                scope.launch {
                                                    val bytes = controller.readCharacteristic(deviceId, svc.uuid, ch.uuid)
                                                    lastRead = bytes
                                                    parsedText = bytes?.let { parseKnownCharacteristic(ch.uuid, it) }
                                                }
                                            }) { Text("Read") }
                                        }
                                        Spacer(Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* ---- Details Form popup ---- */
    if (showForm) {
        Dialog(onDismissRequest = { showForm = false }) {
            Surface(
                tonalElevation = 6.dp,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier.fillMaxSize(0.9f)
            ) {
                Column(Modifier.padding(16.dp).fillMaxSize()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Device Details", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        TextButton(onClick = { showForm = false }) { Text("Close") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Divider()
                    Spacer(Modifier.height(8.dp))

                    if (formRows.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No readable fields found.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(formRows) { (label, value) ->
                                ElevatedCard {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.45f))
                                        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(0.55f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- runtime parsing + helpers ---------------- */

private val gattNames = mapOf(
    "2A37" to "Heart Rate Measurement",
    "2A19" to "Battery Level",
    "2A29" to "Manufacturer Name",
    "2A00" to "Device Name",
    "2A24" to "Model Number",
    "2A25" to "Serial Number",
    "2A26" to "Firmware Revision",
    "2A27" to "Hardware Revision",
    "2A28" to "Software Revision"
)

//private fun uuidShort(uuid: String): String = uuid.takeLast(4).uppercase()
private fun uuidShort(uuid: String): String {
    val l = uuid.lowercase()
    val sigSuffix = "-0000-1000-8000-00805f9b34fb"
    return when {
        // 128-bit Bluetooth SIG UUID -> return the xxxx part
        l.startsWith("0000") && l.endsWith(sigSuffix) -> l.substring(4, 8).uppercase()
        // Already a 16-bit form like "2A29"
        l.length == 4 -> l.uppercase()
        else -> uuid        // vendor/custom UUID: keep full string
    }
}

private suspend fun readDetailsForm(
    controller: BtController,
    deviceId: String,
    services: List<GattServiceInfo>
): List<Pair<String, String>> {
    val wanted = listOf(
        "2A00" to "Device Name",
        "2A29" to "Manufacturer",
        "2A24" to "Model Number",
        "2A25" to "Serial Number",
        "2A26" to "Firmware Revision",
        "2A27" to "Hardware Revision",
        "2A28" to "Software Revision",
        "2A19" to "Battery (%)"
    )
    val rows = mutableListOf<Pair<String, String>>()

    fun findReadable(shortId: String): Pair<String, String>? {
        services.forEach { svc ->
            svc.characteristics.firstOrNull { ch ->
                uuidShort(ch.uuid) == shortId && ch.properties.contains(CharProp.READ)
            }?.let { ch ->
                return svc.uuid to ch.uuid
            }
        }
        return null
    }

    for ((short, label) in wanted) {
        val pair = findReadable(short) ?: continue
        val (svcUuid, chUuid) = pair
        val bytes = controller.readCharacteristic(deviceId, svcUuid, chUuid) ?: continue
        val value = when (short) {
            "2A19" -> bytes.firstOrNull()?.toInt()?.and(0xFF)?.toString() ?: "-"
            else   -> bytes.toUtf8Safe().ifBlank { bytes.toPrettyHex() }
        }
        rows += label to value
    }
    return rows
}

private fun parseKnownCharacteristic(uuid: String, bytes: ByteArray): String? {
    return when (uuidShort(uuid)) {
        "2A37" -> "Heart Rate = ${parseHeartRate(bytes)} bpm"
        "2A19" -> "Battery = ${bytes.firstOrNull()?.toInt()?.and(0xFF) ?: "-"}%"
        "2A29", "2A00", "2A24", "2A25", "2A26", "2A27", "2A28" -> {
            val text = bytes.toUtf8Safe()
            if (text.isNotBlank()) "${gattNames[uuidShort(uuid)] ?: uuidShort(uuid)} = $text" else null
        }
        else -> null
    }
}

private fun ByteArray.toPrettyHex(): String =
    joinToString(" ") { b -> b.toUByte().toString(16).padStart(2, '0').uppercase() }

private fun ByteArray.toUtf8Safe(): String =
    try { decodeToString() } catch (_: Throwable) { "" }

private fun parseHeartRate(bytes: ByteArray): Int {
    if (bytes.isEmpty()) return -1
    val flags = bytes[0].toInt()
    val is16 = (flags and 0x01) != 0
    return if (!is16) {
        if (bytes.size >= 2) bytes[1].toInt() and 0xFF else -1
    } else {
        if (bytes.size >= 3) {
            (bytes[1].toInt() and 0xFF) or ((bytes[2].toInt() and 0xFF) shl 8)
        } else -1
    }
}