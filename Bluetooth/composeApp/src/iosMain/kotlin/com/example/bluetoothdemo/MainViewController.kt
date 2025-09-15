package com.example.bluetoothdemo

import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Compose iOS host for your common App().
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    val scope = rememberCoroutineScope()
    val controller = remember { IosBtController(scope) }
    App(controller)
}