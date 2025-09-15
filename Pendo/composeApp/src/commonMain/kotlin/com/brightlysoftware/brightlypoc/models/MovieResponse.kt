package com.brightlysoftware.brightlypoc.models

import dev.tmapps.konnection.NetworkConnection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieResponse(
    val page: Int,
    val results: List<Movie>,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("total_results")
    val totalResults: Int
){
    companion object {
        const val MAX_PAGES = 3// Your limit
    }

    val hasReachedLimit: Boolean
        get() = page >= MAX_PAGES || page >= totalPages
}

@Serializable
data class Movie(
    val adult: Boolean,
    @SerialName("backdrop_path")
    val backdropPath: String?,
    @SerialName("genre_ids")
    val genreIds: List<Int>,
    val id: Int,
    @SerialName("original_language")
    val originalLanguage: String,
    @SerialName("original_title")
    val originalTitle: String,
    val overview: String,
    val popularity: Double,
    @SerialName("poster_path")
    val posterPath: String?,
    @SerialName("release_date")
    val releaseDate: String,
    val title: String,
    val video: Boolean,
    @SerialName("vote_average")
    val voteAverage: Double,
    @SerialName("vote_count")
    val voteCount: Int
) {
    // Helper function to get full poster URL
    val fullPosterUrl: String
        get() = if (posterPath != null) {
            "https://image.tmdb.org/t/p/w500$posterPath"
        } else {
            "" // Empty string for null poster paths
        }
}

// UI State for the movie list screen
// models/Movie.kt - Update MovieListUiState
data class MovieListUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val isEndReached: Boolean = false,
    val isOffline: Boolean = false, // NEW: Track offline state
    val isUsingCache: Boolean = false, // NEW: Indicate when showing cached data
    val networkConnection: NetworkConnection? = null
)
