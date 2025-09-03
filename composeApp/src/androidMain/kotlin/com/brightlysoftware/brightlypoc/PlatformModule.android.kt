package com.brightlysoftware.brightlypoc.di

import android.content.Context
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import org.koin.dsl.module

actual fun platformModule() = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<Context> { get() }
}