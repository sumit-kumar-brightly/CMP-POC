package com.brightlysoftware.brightlypoc.remote


import com.brightlysoftware.brightlypoc.models.MovieResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class MovieApi(private val httpClient: HttpClient) {

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3"
        private const val API_KEY = "55ae4dfe6754e78d8259020895de74cd" // Make sure this is valid
    }

    suspend fun getPopularMovies(page: Int = 1): Result<MovieResponse> {
        return try {
            println("DEBUG: Making API call for page: $page") // Debug log
            val response = httpClient.get("$BASE_URL/movie/popular") {
                parameter("api_key", API_KEY)
                parameter("page", page)
            }
            val movieResponse: MovieResponse = response.body()
            println("DEBUG: API response - Page: ${movieResponse.page}, Results: ${movieResponse.results.size}, Total Pages: ${movieResponse.totalPages}") // Debug log
            Result.success(movieResponse)
        } catch (e: Exception) {
            println("DEBUG: API error: ${e.message}") // Debug log
            Result.failure(e)
        }
    }
}