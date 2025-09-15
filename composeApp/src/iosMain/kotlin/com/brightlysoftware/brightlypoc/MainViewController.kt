package com.brightlysoftware.brightlypoc

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import com.brightlysoftware.brightlypoc.di.appModule
import org.koin.core.context.startKoin
import com.brightlysoftware.brightlypoc.util.AppStartTimeTracker
import com.brightlysoftware.brightlypoc.util.IosAppStartTime


private val initAppStart: Unit = run {
    AppStartTimeTracker.recordAppStart(IosAppStartTime.startTime)
}
fun MainViewController() = ComposeUIViewController {
//    startKoin {
//        modules(appModule)
//    }
    // Record UI ready time when Compose starts
    LaunchedEffect(Unit) {
        AppStartTimeTracker.recordUiReady()
        initializePendo()
        startPendoSession("bdf640fa-f270-4861-b297-1db8718f2943", accountId = "ACME")
    }
    App()
}
