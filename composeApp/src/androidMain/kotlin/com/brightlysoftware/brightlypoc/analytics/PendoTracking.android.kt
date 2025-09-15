package com.brightlysoftware.brightlypoc.analytics

import sdk.pendo.io.Pendo
import androidx.compose.ui.Modifier
import sdk.pendo.io.pendoTag

actual fun trackScreenView(screenName: String, properties: Map<String, Any>) {
    // Track page/screen view
    Pendo.track("screen_view", properties + mapOf("screen_name" to screenName))
}

actual fun trackEvent(eventName: String, properties: Map<String, Any>) {
    Pendo.track(eventName, properties)
}
actual fun Modifier.pendoTag(tag: String): Modifier {
    return this.pendoTag(tag)
}