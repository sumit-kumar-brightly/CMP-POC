package com.brightlysoftware.brightlypoc.repository

import com.brightlysoftware.brightlypoc.data.local.MovieLocalDataSource
import com.brightlysoftware.brightlypoc.models.Movie
import com.brightlysoftware.brightlypoc.models.MovieResponse
import com.brightlysoftware.brightlypoc.remote.MovieApi
import com.brightlysoftware.brightlypoc.util.NetworkConnectivityService
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

interface MovieRepository {
    suspend fun getPopularMovies(page: Int): Result<MovieResponse>
    suspend fun getCachedMovies(): Result<List<Movie>>
    suspend fun isOfflineMode(): Boolean
}

class MovieRepositoryImpl(
    private val movieApi: MovieApi,
    private val localDataSource: MovieLocalDataSource,
    private val connectivityService: NetworkConnectivityService
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): Result<MovieResponse> {
        if (page > MovieResponse.MAX_PAGES) {
            return Result.success(MovieResponse(page, emptyList(), page, 0)) // Return empty to signal end
        }
        val isConnected = connectivityService.isCurrentlyConnected()

        return if (isConnected) {
            // Online: Try network first
            try {
                val networkResult = movieApi.getPopularMovies(page)
                if (networkResult.isSuccess) {
                    val response = networkResult.getOrThrow()
                    // Cache the results
                    localDataSource.cacheMovies(response.results, page)
                    localDataSource.updateLastSyncTimestamp(Clock.System.now().toEpochMilliseconds())
                    networkResult
                } else {
                    // Network failed, fallback to cache
                    getCachedMoviesAsResponse(page)
                }
            } catch (e: Exception) {
                // Network exception, fallback to cache
                getCachedMoviesAsResponse(page)
            }
        } else {
            // Offline: Use cache only
            getCachedMoviesAsResponse(page)
        }
    }

    override suspend fun getCachedMovies(): Result<List<Movie>> {
        return try {
            val cachedMovies = localDataSource.getAllMoviesFlow().first()
            Result.success(cachedMovies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isOfflineMode(): Boolean {
        return !connectivityService.isCurrentlyConnected()
    }

    private suspend fun getCachedMoviesAsResponse(page: Int): Result<MovieResponse> {
        return try {
            val cachedMovies = localDataSource.getAllMoviesFlow().first()
            // Create a mock response for cached data
            val response = MovieResponse(
                page = page,
                results = cachedMovies,
                totalPages = 1, // Since we're returning all cached data
                totalResults = cachedMovies.size
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("No cached data available and device is offline"))
        }
    }
}
