package com.brightlysoftware.brightlypoc.analytics

import sdk.pendo.io.Pendo

class AndroidPendoAnalytics : PendoAnalytics {
    override fun initialize(apiKey: String) {
        // Android Pendo initialization is typically done in Application.onCreate()
    }

    override fun startSession(
        visitorId: String,
        accountId: String,
        visitorData: Map<String, Any>?,
        accountData: Map<String, Any>?
    ) {
        Pendo.startSession(
            visitorId,
            accountId,
            visitorData ?: emptyMap(),
            accountData ?: emptyMap()
        )
    }

    override fun trackEvent(event: String, properties: Map<String, Any>?) {
        Pendo.track(event, properties ?: emptyMap())
    }

    override fun trackScreen(screenName: String, properties: Map<String, Any>?) {
        val params = mapOf(PendoAnalytics.PARAM_SCREEN_NAME to screenName) + (properties ?: emptyMap())
        Pendo.track(PendoAnalytics.EVENT_SCREEN_VIEW, params)
    }
}
