package com.brightlysoftware.brightlypoc.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.brightlysoftware.brightlypoc.ui.ImageDownloadScreen
import com.brightlysoftware.brightlypoc.ui.MovieListScreen

enum class AppTab(
    val title: String,
    val icon: ImageVector
) {
    Movies("Movies", Icons.Default.Movie),
    Download("Download", Icons.Default.CloudDownload)
}

@Composable
fun TabNavigation() {
    var selectedTab by remember { mutableStateOf(AppTab.Movies) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Content first
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                AppTab.Movies -> MovieListScreen()
                AppTab.Download -> ImageDownloadScreen()
            }
        }

        // TabBar at bottom
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            AppTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) },
                    icon = { Icon(tab.icon, contentDescription = tab.title) }
                )
            }
        }
    }
}