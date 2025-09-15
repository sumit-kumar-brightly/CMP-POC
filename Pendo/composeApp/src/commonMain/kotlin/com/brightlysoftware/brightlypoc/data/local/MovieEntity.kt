package com.brightlysoftware.brightlypoc.data.local

import com.brightlysoftware.brightlypoc.models.Movie
import kotlinx.serialization.json.Json
// Extension functions to convert between API models and database entities
fun Movie.toMovieEntity(page: Int, cachedAt: Long): MovieEntity {
    return MovieEntity(
        id = this.id.toLong(),
        adult = if (this.adult) 1L else 0L,
        backdrop_path = this.backdropPath,
        genre_ids = Json.encodeToString(this.genreIds), // Convert list to JSON string
        original_language = this.originalLanguage,
        original_title = this.originalTitle,
        overview = this.overview,
        popularity = this.popularity,
        poster_path = this.posterPath,
        release_date = this.releaseDate,
        title = this.title,
        video = if (this.video) 1L else 0L,
        vote_average = this.voteAverage,
        vote_count = this.voteCount.toLong(),
        page = page.toLong(),
        cached_at = cachedAt,
        is_favorite = 0L
    )
}

fun MovieEntity.toMovie(): Movie {
    return Movie(
        adult = this.adult == 1L,
        backdropPath = this.backdrop_path,
        genreIds = try {
            Json.decodeFromString<List<Int>>(this.genre_ids)
        } catch (e: Exception) {
            emptyList()
        },
        id = this.id.toInt(),
        originalLanguage = this.original_language,
        originalTitle = this.original_title,
        overview = this.overview,
        popularity = this.popularity,
        posterPath = this.poster_path,
        releaseDate = this.release_date,
        title = this.title,
        video = this.video == 1L,
        voteAverage = this.vote_average,
        voteCount = this.vote_count.toInt()
    )
}

// Type alias for the generated SQLDelight class
typealias MovieEntity = com.brightlysoftware.brightlypoc.database.Movie