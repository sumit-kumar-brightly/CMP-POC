package com.brightlysoftware.brightlypoc

import android.app.Activity
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
@Composable
actual fun getScreenWidth(): Dp = LocalConfiguration.current.screenWidthDp.dp

actual fun reportFullyDrawn(): Unit {
    currentActivity?.reportFullyDrawn()
}

var currentActivity: Activity? = null

// Add this in your Application class or a suitable place that's always active
fun registerActivityLifecycleCallbacks(application: android.app.Application) {
    application.registerActivityLifecycleCallbacks(object :
        android.app.Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) {
            if (activity is ComponentActivity) {
                currentActivity = activity
            }
        }

        override fun onActivityStarted(activity: Activity) {
            if (activity is ComponentActivity) {
                currentActivity = activity
            }
        }

        override fun onActivityResumed(activity: Activity) {
            if (activity is ComponentActivity) {
                currentActivity = activity
            }
        }

        override fun onActivityPaused(activity: Activity) {
            if (currentActivity == activity) {
                currentActivity = null
            }
        }

        override fun onActivityStopped(activity: Activity) {
            if (currentActivity == activity) {
                currentActivity = null
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (currentActivity == activity) {
                currentActivity = null
            }
        }
    })
}

actual suspend fun saveImageToFile(imageData: ByteArray, fileName: String): String {
    return withContext(Dispatchers.IO) {
        val downloadDir = File(getDownloadDirectory())

        // Create directory if it doesn't exist
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        val imageFile = File(downloadDir, fileName)

        try {
            FileOutputStream(imageFile).use { outputStream ->
                outputStream.write(imageData)
                outputStream.flush()
            }

            println("DEBUG ANDROID: Image saved to: ${imageFile.absolutePath}")
            imageFile.absolutePath

        } catch (e: Exception) {
            println("DEBUG ANDROID: Failed to save image: ${e.message}")
            throw Exception("Failed to save image: ${e.message}")
        }
    }
}

actual fun getDownloadDirectory(): String {
    return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        // Use public Downloads directory
        "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/MovieApp"
    } else {
        // This will need to be updated with actual context - see note below
        "/data/data/com.example.movieapp/files/downloads"
    }
}