package com.example.bluetoothdemo

import android.Manifest
import android.os.Build

/**
 * Returns the correct set of runtime permissions for Bluetooth,
 * depending on the Android version.
 */
fun androidBtPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= 31) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
            // Add Manifest.permission.BLUETOOTH_ADVERTISE if you ever advertise
        )
    } else {
        // Pre-Android 12 requires location for BLE scanning
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }