package com.brightlysoftware.brightlypoc.util

import kotlinx.datetime.Clock

object AppStartTimeTracker {
    private var appStartTime: Long = 0
    private var uiReadyTime: Long = 0

    fun recordAppStart(timestamp: Long = 0) {
        if (appStartTime == 0L) {
            appStartTime = if (timestamp > 0) timestamp else Clock.System.now().toEpochMilliseconds()
            println("DEBUG: App start recorded at $appStartTime")
        }
    }

    fun recordUiReady() {
        if (uiReadyTime == 0L) {
            uiReadyTime = Clock.System.now().toEpochMilliseconds()
            println("DEBUG: UI ready recorded at $uiReadyTime")
            println("DEBUG: Cold start time: ${getColdStartTimeMs()}ms")
        }
    }

    fun getColdStartTimeMs(): Long {
        return if (appStartTime > 0 && uiReadyTime > 0) {
            uiReadyTime - appStartTime
        } else {
            println("DEBUG: appStartTime=$appStartTime, uiReadyTime=$uiReadyTime")
            0L
        }
    }

    fun getFormattedColdStartTime(): String {
        val coldStartMs = getColdStartTimeMs()
        return if (coldStartMs > 0) {
            "${coldStartMs}ms"
        } else {
            "Calculating..."
        }
    }

    fun reset() {
        appStartTime = 0
        uiReadyTime = 0
    }
}