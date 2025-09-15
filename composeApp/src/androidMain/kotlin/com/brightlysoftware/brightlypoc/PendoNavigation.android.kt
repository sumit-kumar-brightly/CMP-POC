package com.brightlysoftware.brightlypoc.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import sdk.pendo.io.Pendo

@Composable
actual fun SetupPendoNavigation() {
    val navHostController = rememberNavController()

    DisposableEffect(navHostController) {
        Pendo.setComposeNavigationController(navHostController)

        onDispose {
            Pendo.setComposeNavigationController(null)
        }
    }
}