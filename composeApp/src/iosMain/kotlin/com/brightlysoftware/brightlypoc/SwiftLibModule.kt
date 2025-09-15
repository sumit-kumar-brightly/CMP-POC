package com.brightlysoftware.brightlypoc

import com.brightlysoftware.brightlypoc.analytics.PendoAnalytics
import com.brightlysoftware.brightlypoc.analytics.SwiftLibDependencyFactory
import org.koin.core.KoinApplication
import org.koin.dsl.module

internal fun swiftLibDependenciesModule(factory: SwiftLibDependencyFactory) = module {
    single<PendoAnalytics> { factory.providePendoAnalyticsImpl() }
}

fun KoinApplication.provideSwiftLibDependencyFactory(factory: SwiftLibDependencyFactory) =
    modules(swiftLibDependenciesModule(factory))