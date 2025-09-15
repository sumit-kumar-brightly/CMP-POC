package com.brightlysoftware.brightlypoc.models

data class DownloadMetrics(
    val fileName: String = "",
    val fileSize: Long = 0L, // in bytes
    val downloadedBytes: Long = 0L,
    val downloadSpeed: Double = 0.0, // bytes per second
    val timeElapsed: Long = 0L, // in milliseconds
    val progress: Float = 0f, // 0.0 to 1.0
    val status: DownloadStatus = DownloadStatus.Idle,
    val savedPath: String = "" // Path where file is saved
)

enum class DownloadStatus {
    Idle,
    Downloading,
    Completed,
    Failed,
    Cancelled
}

// UI State for download screen
data class ImageDownloadUiState(
    val downloadMetrics: DownloadMetrics = DownloadMetrics(),
    val isDownloading: Boolean = false,
    val error: String? = null
)