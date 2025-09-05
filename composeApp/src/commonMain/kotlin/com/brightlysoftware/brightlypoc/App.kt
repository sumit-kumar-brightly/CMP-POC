package com.brightlysoftware.brightlypoc

import androidx.compose.runtime.*
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import org.koin.compose.KoinApplication
import com.brightlysoftware.brightlypoc.di.appModule
import com.brightlysoftware.brightlypoc.navigation.TabNavigation

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        setSingletonImageLoaderFactory { context ->
            getAsyncImageLoader(context)
        }
        TabNavigation()
    }
}

fun getAsyncImageLoader(context: PlatformContext): ImageLoader {
    return ImageLoader.Builder(context)
        .crossfade(true)
        .logger(DebugLogger())
        .diskCache { null } // explicitly disables disk cache
        .memoryCache {
            coil3.memory.MemoryCache.Builder()
                .maxSizeBytes(160 * 1024 * 1024)
                .build()
        }
        .build()
}