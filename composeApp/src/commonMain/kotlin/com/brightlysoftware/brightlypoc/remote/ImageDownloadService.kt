package com.brightlysoftware.brightlypoc.remote

import com.brightlysoftware.brightlypoc.models.DownloadMetrics
import com.brightlysoftware.brightlypoc.models.DownloadStatus
import com.brightlysoftware.brightlypoc.saveImageToFile
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

class ImageDownloadService(
    private val httpClient: HttpClient
) {

    companion object {
        private const val IMAGE_URL = "https://upload.wikimedia.org/wikipedia/commons/3/3f/Fronalpstock_big.jpg" // Single large image
        private const val FILE_NAME = "downloaded_image.jpg"
    }

    suspend fun downloadImageWithProgress(imageUrl: String): Flow<DownloadMetrics> = flow {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val fileName = "downloaded_image.jpg"
        try {
            emit(
                DownloadMetrics(
                    fileName = FILE_NAME,
                    status = DownloadStatus.Downloading
                )
            )

            val response: HttpResponse = httpClient.get(imageUrl)
            val contentLength = response.contentLength() ?: 0L
            val channel: ByteReadChannel = response.bodyAsChannel()

            var downloadedBytes = 0L
            val buffer = ByteArray(8192) // 8KB buffer
            val imageData = mutableListOf<Byte>()

            while (!channel.isClosedForRead) {
                val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                if (bytesRead == -1) break

                // Collect the data
                for (i in 0 until bytesRead) {
                    imageData.add(buffer[i])
                }

                downloadedBytes += bytesRead
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val timeElapsed = currentTime - startTime

                val progress = if (contentLength > 0) {
                    downloadedBytes.toFloat() / contentLength.toFloat()
                } else {
                    0f
                }

                val downloadSpeed = if (timeElapsed > 0) {
                    (downloadedBytes.toDouble() / timeElapsed.toDouble()) * 1000.0 // bytes per second
                } else {
                    0.0
                }

                emit(
                    DownloadMetrics(
                        fileName = FILE_NAME,
                        fileSize = contentLength,
                        downloadedBytes = downloadedBytes,
                        downloadSpeed = downloadSpeed,
                        timeElapsed = timeElapsed,
                        progress = progress,
                        status = DownloadStatus.Downloading
                    )
                )
            }

            // Save the file
            val savedPath = saveImageToFile(imageData.toByteArray(), FILE_NAME)

            // Final metrics
            val finalTime = Clock.System.now().toEpochMilliseconds()
            val totalTime = finalTime - startTime
            val finalSpeed = if (totalTime > 0) {
                (downloadedBytes.toDouble() / totalTime.toDouble()) * 1000.0
            } else {
                0.0
            }

            emit(
                DownloadMetrics(
                    fileName = FILE_NAME,
                    fileSize = contentLength,
                    downloadedBytes = downloadedBytes,
                    downloadSpeed = finalSpeed,
                    timeElapsed = totalTime,
                    progress = 1f,
                    status = DownloadStatus.Completed,
                    savedPath = savedPath
                )
            )

        } catch (e: Exception) {
            val errorTime = Clock.System.now().toEpochMilliseconds()
            emit(
                DownloadMetrics(
                    fileName = FILE_NAME,
                    timeElapsed = errorTime - startTime,
                    status = DownloadStatus.Failed
                )
            )
            throw e
        }
    }
}