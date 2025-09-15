package com.brightlysoftware.brightlypoc.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.brightlysoftware.brightlypoc.analytics.PendoAnalytics
import com.brightlysoftware.brightlypoc.analytics.pendoTag
import com.brightlysoftware.brightlypoc.ui.ImageDownloadScreen
import com.brightlysoftware.brightlypoc.ui.MovieListScreen
import com.brightlysoftware.brightlypoc.ui.ChartScreen
import com.brightlysoftware.brightlypoc.analytics.trackScreenView
import com.brightlysoftware.brightlypoc.analytics.trackEvent
import org.koin.compose.koinInject

enum class AppTab(
    val title: String,
    val icon: ImageVector
) {
    Movies("Home", Icons.Default.Movie),
    Download("Download", Icons.Default.CloudDownload),
    ChartScreen("DashBoard", Icons.Rounded.Menu)
}

@Composable
expect fun SetupPendoNavigation()

@Composable
fun TabNavigation() {
    SetupPendoNavigation()
    val pendoAnalytics = koinInject<PendoAnalytics>()
    var selectedTab by remember { mutableStateOf(AppTab.Movies) }
    LaunchedEffect(selectedTab) {
        pendoAnalytics.trackScreen(
            screenName = selectedTab.title,
            properties = mapOf(
                "tab_index" to selectedTab.ordinal.toString(),
                "tab_name" to selectedTab.name
            )
        )
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        // Content first
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                AppTab.Movies -> MovieListScreen()
                AppTab.Download -> ImageDownloadScreen()
                AppTab.ChartScreen -> ChartScreen()
            }
        }

        // TabBar at bottom
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
                .pendoTag("bottom_tab_bar")
        ) {
            AppTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick =
                        {
                           selectedTab = tab
                            pendoAnalytics.trackEvent(
                              "tab_clicked",
                              mapOf(
                                  "tab_name" to tab.name,
                                  "previous_tab" to selectedTab.name
                             )
                           )
                        },

                    text = { Text(tab.title) },
                    icon = { Icon(tab.icon, contentDescription = tab.title) },
                    modifier = Modifier.pendoTag("tab_${tab.name.lowercase()}")
                )
            }
        }
    }
}