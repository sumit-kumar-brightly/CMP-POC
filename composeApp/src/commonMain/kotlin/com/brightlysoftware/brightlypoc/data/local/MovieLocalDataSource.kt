package com.brightlysoftware.brightlypoc.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.brightlysoftware.brightlypoc.models.Movie
import com.brightlysoftware.brightlypoc.database.MovieDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class MovieLocalDataSource(database: MovieDatabase) {
    private val queries = database.movieDatabaseQueries

    // Get all cached movies as Flow
    fun getAllMoviesFlow(): Flow<List<Movie>> {
        return queries.selectAllMovies()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { movieEntities ->
                movieEntities.map { it.toMovie() }
            }
    }

    // Get movies count
    suspend fun getMoviesCount(): Long = withContext(Dispatchers.IO) {
        queries.selectMoviesCount().executeAsOne()
    }

    // Cache movies from a specific page
    suspend fun cacheMovies(movies: List<Movie>, page: Int) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()

        // Clear existing movies from this page first
        queries.deleteMoviesByPage(page.toLong())

        // Insert new movies
        movies.forEach { movie ->
            val entity = movie.toMovieEntity(page, currentTime)
            queries.insertMovie(
                entity.id, entity.adult, entity.backdrop_path, entity.genre_ids,
                entity.original_language, entity.original_title, entity.overview,
                entity.popularity, entity.poster_path, entity.release_date,
                entity.title, entity.video, entity.vote_average, entity.vote_count,
                entity.page, entity.cached_at, entity.is_favorite
            )
        }
    }

    // Get last sync timestamp
    suspend fun getLastSyncTimestamp(): Long = withContext(Dispatchers.IO) {
        try {
            queries.selectCacheMetadata("last_sync_timestamp")
                .executeAsOne()
                .value_
                .toLong()
        } catch (e: Exception) {
            0L
        }
    }

    // Update last sync timestamp
    suspend fun updateLastSyncTimestamp(timestamp: Long) = withContext(Dispatchers.IO) {
        queries.insertCacheMetadata("last_sync_timestamp", timestamp.toString(), timestamp)
    }

    // Check if cache is stale (older than 1 hour)
    suspend fun isCacheStale(): Boolean = withContext(Dispatchers.IO) {
        val lastSync = getLastSyncTimestamp()
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val oneHour = 60 * 60 * 1000L // 1 hour in milliseconds

        (currentTime - lastSync) > oneHour
    }

    // Clear old cache data
    suspend fun clearOldCache() = withContext(Dispatchers.IO) {
        val oneDayAgo = Clock.System.now().toEpochMilliseconds() - (24 * 60 * 60 * 1000L) // 24 hours ago
        queries.deleteOldMovies(oneDayAgo)
    }

    // Clear all cached data
    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        queries.clearAllMovies()
    }

    // Check if cache is empty
    suspend fun isCacheEmpty(): Boolean = withContext(Dispatchers.IO) {
        getMoviesCount() == 0L
    }
}
