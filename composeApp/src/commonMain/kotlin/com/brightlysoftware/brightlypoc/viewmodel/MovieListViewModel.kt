package com.brightlysoftware.brightlypoc.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.example.movieapp.data.models.Movie
import com.brightlysoftware.brightlypoc.models.MovieListUiState
import com.brightlysoftware.brightlypoc.repository.MovieRepository
import com.brightlysoftware.brightlypoc.util.Paginator
import kotlinx.coroutines.launch

class MovieListViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    var state by mutableStateOf(MovieListUiState())
        private set

    private val paginator = Paginator(
        initialKey = 1,
        onLoadUpdated = { isLoading ->
            println("DEBUG: Pagination loading: $isLoading") // Debug log
            state = state.copy(isLoadingMore = isLoading)
        },
        onRequest = { page ->
            println("DEBUG: Requesting page: $page") // Debug log
            repository.getPopularMovies(page)
                .map { it.results }
        },
        getNextKey = { movies ->
            val nextPage = state.currentPage + 1
            println("DEBUG: Next page will be: $nextPage") // Debug log
            nextPage
        },
        onError = { error ->
            println("DEBUG: Pagination error: ${error?.message}") // Debug log
            state = state.copy(
                error = error?.message ?: "Unknown error occurred",
                isLoadingMore = false
            )
        },
        onSuccess = { movies, newPage ->
            println("DEBUG: Success - Page: $newPage, Movies count: ${movies.size}")

            state = state.copy(
                movies = if (state.currentPage == 1) {
                    movies  // First page - replace
                } else {
                    state.movies + movies  // Append for subsequent pages
                },
                currentPage = newPage,
                isEndReached = movies.isEmpty(),
                error = null,
                isLoadingMore = false
            )

            println("DEBUG: After update - Total movies: ${state.movies.size}")
        }
    )

    init {
        loadInitialMovies()
    }

    fun loadNextMovies() {
        println("DEBUG: loadNextMovies called - Current state: isLoadingMore=${state.isLoadingMore}, isEndReached=${state.isEndReached}")
        if (!state.isLoadingMore && !state.isEndReached) {
            viewModelScope.launch {
                paginator.loadNextItems()
            }
        }
    }

    private fun loadInitialMovies() {
        state = state.copy(isLoading = true)
        viewModelScope.launch {
            try {
                paginator.loadNextItems()
            } finally {
                state = state.copy(isLoading = false)
            }
        }
    }

    fun retry() {
        if (state.movies.isEmpty()) {
            loadInitialMovies()
        } else {
            loadNextMovies()
        }
    }

    fun clearError() {
        state = state.copy(error = null)
    }
}