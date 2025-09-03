package com.brightlysoftware.brightlypoc.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightlysoftware.brightlypoc.models.DownloadStatus
import com.brightlysoftware.brightlypoc.models.ImageDownloadUiState
import com.brightlysoftware.brightlypoc.remote.ImageDownloadService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ImageDownloadViewModel(
    private val downloadService: ImageDownloadService
) : ViewModel() {

    var state by mutableStateOf(ImageDownloadUiState())
        private set

    private var downloadJob: Job? = null

    fun startDownload(url: String) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            state = state.copy(
                isDownloading = true,
                error = null
            )

            downloadService.downloadImageWithProgress(url)
                .catch { exception ->
                    state = state.copy(
                        isDownloading = false,
                        error = exception.message ?: "Download failed"
                    )
                }
                .collect { metrics ->
                    state = state.copy(
                        downloadMetrics = metrics,
                        isDownloading = metrics.status == DownloadStatus.Downloading
                    )
                }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        state = state.copy(
            isDownloading = false,
            downloadMetrics = state.downloadMetrics.copy(
                status = DownloadStatus.Cancelled
            )
        )
    }

    fun clearError() {
        state = state.copy(error = null)
    }

    fun resetDownload() {
        state = ImageDownloadUiState()
    }
}