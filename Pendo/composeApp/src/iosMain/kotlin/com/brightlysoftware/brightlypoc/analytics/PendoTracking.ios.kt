package com.brightlysoftware.brightlypoc.analytics

import androidx.compose.ui.Modifier
import platform.Foundation.NSMutableDictionary
import platform.Foundation.*
//import cocoapods.Pendo.PendoManager
import kotlinx.cinterop.ExperimentalForeignApi
@OptIn(ExperimentalForeignApi::class)
actual fun trackScreenView(screenName: String, properties: Map<String, Any>) {
    println("iOS Screen View: $screenName")

    val pendoProperties = properties.toMutableMap()
    pendoProperties["screenName"] = screenName
    pendoProperties["platform"] = "iOS"
    pendoProperties["timestamp"] = platform.Foundation.NSDate().timeIntervalSince1970

//    val nsDict = pendoProperties.toNSDictionary()
//    PendoManager.sharedManager().track("Screen_Viewed", null)
}

@OptIn(ExperimentalForeignApi::class)
actual fun trackEvent(eventName: String, properties: Map<String, Any>) {
    println("iOS Event: $eventName")

    val pendoProperties = properties.toMutableMap()
    pendoProperties["platform"] = "iOS"
    pendoProperties["timestamp"] = platform.Foundation.NSDate().timeIntervalSince1970

//    val nsDict = pendoProperties.toNSDictionary()
//    PendoManager.sharedManager().track(eventName, properties = null)
}

actual fun Modifier.pendoTag(tag: String): Modifier {
    // For iOS, we can't directly modify Compose modifiers for Pendo tagging
    // Pendo iOS uses view hierarchy inspection instead
    return this
}

// Helper extension to convert Map to NSDictionary
//@OptIn(ExperimentalForeignApi::class)
//public fun Map<String, Any>.toNSDictionary(): NSDictionary {
////    val dict = NSMutableDictionary()
////    this.forEach { (key, value) ->
////        when (value) {
////            is String -> dict.setObject(value, key)
////            is Number -> dict.setObject(value, key)
////            is Boolean -> dict.setObject(value, key)
////            else -> dict.setObject(value.toString(), key)
////        }
////    }
////    return dict
//}