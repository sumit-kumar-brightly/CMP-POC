package com.brightlysoftware.brightlypoc

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import platform.UIKit.UIDevice
import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.UIKit.UIApplication

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getScreenWidth(): Dp = LocalWindowInfo.current.containerSize.width.dp


actual fun reportFullyDrawn() {
    // no-op
}
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun saveImageToFile(imageData: ByteArray, fileName: String): String {
    return withContext(Dispatchers.Default) {
        val downloadDir = getDownloadDirectory()
        val filePath = "$downloadDir/$fileName"

        try {
            val fileManager = NSFileManager.defaultManager

            // Allocate native boolean to receive isDirectory flag
            val isDirectoryPtr = nativeHeap.alloc<BooleanVar>()
            try {
                val dirExists = fileManager.fileExistsAtPath(downloadDir, isDirectoryPtr.ptr)
                val isDirectory = isDirectoryPtr.value

                if (!dirExists || !isDirectory) {
                    val success = fileManager.createDirectoryAtPath(
                        path = downloadDir,
                        withIntermediateDirectories = true,
                        attributes = null,
                        error = null
                    )
                    if (!success) {
                        throw Exception("Failed to create directory at $downloadDir")
                    }
                }
            } finally {
                // Free native allocation to prevent memory leaks
                nativeHeap.free(isDirectoryPtr)
            }

            // Convert ByteArray to NSData with usePinned (optIn required)
            val nsData = imageData.usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = imageData.size.toULong()
                )
            }

            // Write NSData to file
            val success = nsData.writeToFile(filePath, atomically = true)

            if (success) {
                println("DEBUG iOS: Image saved to: $filePath")
                filePath
            } else {
                throw Exception("Failed to write image data to file")
            }
        } catch (e: Exception) {
            println("DEBUG iOS: Failed to save image: ${e.message}")
            throw Exception("Failed to save image: ${e.message}")
        }
    }
}

actual fun getDownloadDirectory(): String {
    // Get Documents directory path
    val documentsPath = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).firstOrNull() as? String ?: ""

    return "$documentsPath/MovieApp/Downloads"
}

