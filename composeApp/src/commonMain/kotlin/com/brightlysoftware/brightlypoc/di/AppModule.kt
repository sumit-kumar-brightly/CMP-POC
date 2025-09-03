package com.brightlysoftware.brightlypoc.di

import com.brightlysoftware.brightlypoc.remote.ImageDownloadService
import com.brightlysoftware.brightlypoc.remote.MovieApi
import com.brightlysoftware.brightlypoc.repository.MovieRepository
import com.brightlysoftware.brightlypoc.repository.MovieRepositoryImpl
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

expect fun platformModule(): Module

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
        }
    }

    single { MovieApi(get()) }
    single { ImageDownloadService(get()) } // Add this
}

val repositoryModule = module {
    single<MovieRepository> { MovieRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { MovieListViewModel(get()) }
    viewModel { ImageDownloadViewModel(get()) } // Add this
}

val appModule = listOf(
    platformModule(),
    networkModule,
    repositoryModule,
    viewModelModule
)