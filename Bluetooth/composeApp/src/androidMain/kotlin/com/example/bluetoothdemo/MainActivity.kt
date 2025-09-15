package com.example.bluetoothdemo

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

class MainActivity : ComponentActivity() {

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Optionally inspect grants here */ }

    private val btOnLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* User may accept/deny enabling BT */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request runtime permissions that are not yet granted
        val needed = androidBtPermissions().filter {
            ContextCompat.checkSelfPermission(this, it) != PermissionChecker.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permLauncher.launch(needed.toTypedArray())
        }

        // Prompt to enable Bluetooth if it's OFF (optional but helpful)
        if (BluetoothAdapter.getDefaultAdapter()?.isEnabled != true) {
            btOnLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        setContent {
            val scope = rememberCoroutineScope()
            val controller: BtController = remember { AndroidBtController(this, scope) }
            MaterialTheme { App(controller) }
        }
    }
}