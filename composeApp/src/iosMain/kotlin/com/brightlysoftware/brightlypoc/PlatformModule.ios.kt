package com.brightlysoftware.brightlypoc.di

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*
import org.koin.dsl.module

actual fun platformModule() = module {
    single<HttpClientEngine> { Darwin.create() }
}