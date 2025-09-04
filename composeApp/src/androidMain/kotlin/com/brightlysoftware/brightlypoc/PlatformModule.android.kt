package com.brightlysoftware.brightlypoc.di

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import com.brightlysoftware.brightlypoc.data.local.DatabaseDriverFactory
import com.brightlysoftware.brightlypoc.data.local.MovieLocalDataSource
import com.brightlysoftware.brightlypoc.database.MovieDatabase
import com.brightlysoftware.brightlypoc.util.NetworkConnectivityService

actual fun platformModule() = module {
    single<HttpClientEngine> { OkHttp.create() }
//    single<Context> { get() }
    // Database setup for Android
    single { NetworkConnectivityService() }
    single { DatabaseDriverFactory(androidContext()) } // Explicitly pass Android context
    single {
        val driver = get<DatabaseDriverFactory>().createDriver()
        MovieDatabase(driver)
    }
    single { MovieLocalDataSource(get()) }
}