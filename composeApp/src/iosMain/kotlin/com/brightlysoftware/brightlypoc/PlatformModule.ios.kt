package com.brightlysoftware.brightlypoc.di

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*
import org.koin.dsl.module
import com.brightlysoftware.brightlypoc.data.local.DatabaseDriverFactory
import com.brightlysoftware.brightlypoc.data.local.MovieLocalDataSource
import com.brightlysoftware.brightlypoc.database.MovieDatabase
import com.brightlysoftware.brightlypoc.util.NetworkConnectivityService

actual fun platformModule() = module {
    single<HttpClientEngine> { Darwin.create() }
    // Database setup for iOS
    single { DatabaseDriverFactory() } // No parameters for iOS
    single { NetworkConnectivityService() }
    single {
        val driver = get<DatabaseDriverFactory>().createDriver()
        MovieDatabase(driver)
    }
    single { MovieLocalDataSource(get()) }
}