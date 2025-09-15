package com.brightlysoftware.brightlypoc.util

import kotlinx.datetime.Clock

object AndroidAppStartTime {
    // Static initializer - runs when class is first loaded
    val startTime: Long = Clock.System.now().toEpochMilliseconds().also {
        println("DEBUG ANDROID: App start time recorded at $it")
    }

    init {
        println("DEBUG ANDROID: AndroidAppStartTime object initialized")
    }
}