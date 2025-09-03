package com.brightlysoftware.brightlypoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.brightlysoftware.brightlypoc.util.AppStartTimeTracker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // Mark app as ready when UI is composed
            AppStartTimeTracker.recordUiReady()
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}