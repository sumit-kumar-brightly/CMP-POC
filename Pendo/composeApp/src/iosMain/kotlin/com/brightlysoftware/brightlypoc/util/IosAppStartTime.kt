package com.brightlysoftware.brightlypoc.util

import kotlinx.datetime.Clock
import platform.CoreFoundation.CFAbsoluteTimeGetCurrent
import platform.Foundation.NSDate

object IosAppStartTime {
    // Record as early as possible when this object is accessed
    val startTime: Long by lazy {
        val time = Clock.System.now().toEpochMilliseconds()
        println("DEBUG iOS: App start time recorded at $time")
        time
    }
}