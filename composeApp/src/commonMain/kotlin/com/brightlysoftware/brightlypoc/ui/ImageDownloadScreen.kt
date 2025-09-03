package com.brightlysoftware.brightlypoc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brightlysoftware.brightlypoc.models.DownloadStatus
import com.brightlysoftware.brightlypoc.viewmodel.ImageDownloadViewModel
import org.koin.compose.koinInject
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import com.brightlysoftware.brightlypoc.util.AppStartTimeTracker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDownloadScreen(
    viewModel: ImageDownloadViewModel = koinInject()
) {
    val state = viewModel.state
// Editable image URL state, prepopulated with large image URL
    var imageUrl by remember {
        mutableStateOf("https://upload.wikimedia.org/wikipedia/commons/3/3f/Fronalpstock_big.jpg")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Image Download")
                        Text(
                         "Cold Start: ${AppStartTimeTracker.getFormattedColdStartTime()}",
                         style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                     }
                        },

                actions = {
                    if (state.downloadMetrics.status == DownloadStatus.Completed ||
                        state.downloadMetrics.status == DownloadStatus.Failed) {
                        IconButton(onClick = { viewModel.resetDownload() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset"
                            )
                        }
                    }
                }
            )
            // Cold Start Time Banner
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Download Info Card
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Download Large Image",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // URL input field (editable)
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                placeholder = { Text("Enter image URL to download...") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isDownloading,
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                }
            )

            // Download Button
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.isDownloading) {
                        Button(
                            onClick = { viewModel.cancelDownload() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel Download")
                        }
                    } else {
                        Button(
                            onClick = { if (imageUrl.isNotBlank()) viewModel.startDownload(imageUrl.trim()) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.downloadMetrics.status != DownloadStatus.Completed
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Download")
                        }
                    }
                }
            }

            // Download Progress and Metrics
            if (state.downloadMetrics.status != DownloadStatus.Idle) {
                DownloadMetricsCard(metrics = state.downloadMetrics)
            }

            // Error Display
            if (state.error != null) {
                ErrorCard(
                    error = state.error,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}

@Composable
private fun DownloadMetricsCard(
    metrics: com.brightlysoftware.brightlypoc.models.DownloadMetrics
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Download Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            if (metrics.status == DownloadStatus.Downloading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Metrics
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricRow("Status", metrics.status.name)
                MetricRow("File Name", metrics.fileName)

                if (metrics.fileSize > 0) {
                    MetricRow("File Size", formatBytes(metrics.fileSize))
                }

                if (metrics.downloadedBytes > 0) {
                    MetricRow("Downloaded", formatBytes(metrics.downloadedBytes))
                }

                if (metrics.downloadSpeed > 0) {
                    MetricRow("Speed", "${formatBytes(metrics.downloadSpeed.toLong())}/s")
                }

                if (metrics.timeElapsed > 0) {
                    MetricRow("Time Taken", formatTime(metrics.timeElapsed))
                }

                // Show saved path when completed
                if (metrics.status == DownloadStatus.Completed && metrics.savedPath.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "✅ Download Completed!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Saved to:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Text(
                                text = metrics.savedPath,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ColdStartTimeBanner(coldStartTime: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Cold Start Time",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "App Cold Start: $coldStartTime",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

// Utility functions
private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${(kb * 10).roundToLong() / 10.0} KB"
    val mb = kb / 1024.0
    if (mb < 1024) return "${(mb * 10).roundToLong() / 10.0} MB"
    val gb = mb / 1024.0
    return "${(gb * 10).roundToLong() / 10.0} GB"
}

private fun formatTime(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60

    return when {
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}