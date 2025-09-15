package com.brightlysoftware.brightlypoc.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

object AppInitializer {
    fun initialize(onKoinStart: (KoinApplication.() -> Unit)? = null) {
        startKoin {
            onKoinStart?.invoke(this)
            modules(
                appModule
                // Add other modules you currently use
            )
        }
    }
}