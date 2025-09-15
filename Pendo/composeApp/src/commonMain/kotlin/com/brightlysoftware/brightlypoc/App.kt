package com.brightlysoftware.brightlypoc

import androidx.compose.runtime.*
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.brightlysoftware.brightlypoc.analytics.PendoAnalytics
import org.koin.compose.KoinApplication
import com.brightlysoftware.brightlypoc.di.appModule
import com.brightlysoftware.brightlypoc.navigation.TabNavigation
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun App() {

    val pendoAnalytics = koinInject<PendoAnalytics>()

    LaunchedEffect(Unit) {
        pendoAnalytics.initialize("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhY2VudGVyIjoidXMiLCJrZXkiOiJmNzM1YjMzNmRkYmM2ODU5MWFjMDkwNmE3ZDg3MGYzYjAwNzQ1Y2NkMWFlZDkxN2I1YTgzYmNjY2NhOGE3YzVmMmM1MDNjZGZiODNlNjQxYjI1MWM0ZjI5MDc3OTJiMDhjNTZlYmRkMmIxMTI0YWZmMmIzN2IwNjI4MTNiMzM5N2JmMTRlODZkYjQzZmE2NWExMmFkMmFkMDZjYWQ1ZmM4Zjc0Mjg3NjBlODZjMGI2NzVlOWIwMTY0NDc5NTM0MmQ4OTQ1MzNjODc5NTUyYTMyNjY3NDQyMzFiYTU5M2RiMDIzMjM4MzVhZGEyMDI5YmVkNWY2ZGUzMjBkNjk3OWI0NDU2NDk2NjM2ZTc5ZWFmM2U1YzkwYjhkNzZkZjQyNDIuMjM0NDYwZjgxZWZmZjhmNDE1ZDU1ZTdmOTAyN2Y0NGYuMGNmM2NkYjM4ZTQ3ZGIyMTliN2UxOWFkM2JkZDEwZmY4NDIxYWY3ZTBjOWVjZDM1MDVkZTliNWU2MmE4YzI5ZSJ9.TZ0zFVTqcpIWCTIYsF_05xJcYCicoRGP4zAKyoINUp2CH8rlUHbxjcTbcOjuCT8-Q1yC1H2SQwp2P9uoZmaTGOy2Owi46SNNjw-U2U6opqQXTtuYJ7sA03fLWX7wDBMDKn1LDX2ECL-5JR_ktW0RMJ_6QPN3RJPjJp6UGWy2JrA")
        pendoAnalytics.startSession(
            visitorId = "bdf640fa-f270-4861-b297-1db8718f2943",
            accountId = "Account Id",
            visitorData = mapOf("product" to "MME"),
            accountData = mapOf("name" to "Brightly Internal")
        )
    }

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