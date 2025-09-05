
package com.brightlysoftware.brightlypoc.di

import com.brightlysoftware.brightlypoc.remote.ImageDownloadService
import com.brightlysoftware.brightlypoc.remote.MovieApi
import com.brightlysoftware.brightlypoc.repository.MovieRepository
import com.brightlysoftware.brightlypoc.repository.MovieRepositoryImpl
import com.brightlysoftware.brightlypoc.util.NetworkConnectivityService
import com.brightlysoftware.brightlypoc.viewmodel.ImageDownloadViewModel
import com.brightlysoftware.brightlypoc.viewmodel.MovieListViewModel
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.observer.ResponseObserver

expect fun platformModule(): Module

// Connectivity module
val connectivityModule = module {
    single { NetworkConnectivityService() }
}

val networkModule = module {
    single<HttpClient> {
        HttpClient(get<HttpClientEngine>()) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            install(ResponseObserver) {
                onResponse { response ->
                    val latencyMs = response.responseTime.timestamp - response.requestTime.timestamp
                    println("HTTP ${response.call}: Latency = $latencyMs ms")
                }
            }
        }
    }

    single { MovieApi(get()) }
    single { ImageDownloadService(get()) }
}

val repositoryModule = module {
    single<MovieRepository> {
        MovieRepositoryImpl(
            movieApi = get(),
            localDataSource = get(),
            connectivityService = get()
        )
    }
}

val viewModelModule = module {
    viewModel { MovieListViewModel(get(), get()) }
    viewModel { ImageDownloadViewModel(get()) }
}

val appModule = listOf(
    platformModule(), // Now includes database setup per platform
    connectivityModule,
    networkModule,
    repositoryModule,
    viewModelModule
)
