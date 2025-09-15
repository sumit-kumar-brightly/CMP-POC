package com.brightlysoftware.brightlypoc.analytics

interface PendoAnalytics {
    fun initialize(apiKey: String)
    fun startSession(
        visitorId: String,
        accountId: String,
        visitorData: Map<String, Any>? = null,
        accountData: Map<String, Any>? = null
    )
    fun trackEvent(event: String, properties: Map<String, Any>? = null)
    fun trackScreen(screenName: String, properties: Map<String, Any>? = null)

    companion object {
        const val EVENT_SCREEN_VIEW = "screen_view"
        const val PARAM_SCREEN_NAME = "screen_name"
    }
}

// Extension function for convenient screen tracking
fun PendoAnalytics.logScreenView(screenName: String, params: Map<String, Any>? = null) {
    trackScreen(screenName, params)
}