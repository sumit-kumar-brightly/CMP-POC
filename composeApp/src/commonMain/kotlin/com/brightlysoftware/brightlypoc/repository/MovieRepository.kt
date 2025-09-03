package com.brightlysoftware.brightlypoc.repository

import com.brightlysoftware.brightlypoc.models.MovieResponse
import com.brightlysoftware.brightlypoc.remote.MovieApi

interface MovieRepository {
    suspend fun getPopularMovies(page: Int): Result<MovieResponse>
}

class MovieRepositoryImpl(
    private val movieApi: MovieApi
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): Result<MovieResponse> {
        return movieApi.getPopularMovies(page)
    }
}