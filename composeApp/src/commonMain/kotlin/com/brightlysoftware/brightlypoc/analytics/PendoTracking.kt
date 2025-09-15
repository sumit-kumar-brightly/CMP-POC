package com.brightlysoftware.brightlypoc.analytics
import androidx.compose.ui.Modifier
expect fun trackScreenView(screenName: String, properties: Map<String, Any> = emptyMap())
expect fun trackEvent(eventName: String, properties: Map<String, Any> = emptyMap())
expect fun Modifier.pendoTag(tag: String): Modifier