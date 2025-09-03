package com.brightlysoftware.brightlypoc

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import org.koin.core.module.Module
interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun getScreenWidth(): Dp

expect fun reportFullyDrawn()

expect suspend fun saveImageToFile(imageData: ByteArray, fileName: String): String
expect fun getDownloadDirectory(): String