package com.brightlysoftware.brightlypoc.analytics

interface SwiftLibDependencyFactory {
    fun providePendoAnalyticsImpl(): PendoAnalytics
}