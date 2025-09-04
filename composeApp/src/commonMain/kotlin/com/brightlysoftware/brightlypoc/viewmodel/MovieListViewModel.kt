package com.brightlysoftware.brightlypoc.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightlysoftware.brightlypoc.models.MovieListUiState
import com.brightlysoftware.brightlypoc.repository.MovieRepository
import com.brightlysoftware.brightlypoc.util.NetworkConnectivityService
import com.brightlysoftware.brightlypoc.util.Paginator
import kotlinx.coroutines.launch

class MovieListViewModel(
    private val repository: MovieRepository,
    private val connectivityService: NetworkConnectivityService,
    private val maxPages: Int
) : ViewModel() {

    var state by mutableStateOf(MovieListUiState(maxPages = maxPages))
        private set
    private val paginator = Paginator(
        initialKey = 1,
        onLoadUpdated = { isLoading ->
            state = state.copy(isLoadingMore = isLoading)
        },
        onRequest = { page ->
            repository.getPopularMovies(page)
                .map { it.results }
        },
        getNextKey = { movies ->
            state.currentPage + 1
        },
        onError = { error ->
            state = state.copy(
                error = error?.message ?: "Unknown error occurred",
                isLoadingMore = false
            )
        },
        onSuccess = { movies, newPage ->
            val isOffline = repository.isOfflineMode()
            state = state.copy(
                movies = if (state.currentPage == 1) {
                    movies // First page - replace
                } else {
                    state.movies + movies // Append for subsequent pages
                },
                currentPage = newPage,
                isEndReached = movies.isEmpty() || isOffline || newPage > state.maxPages, // End pagination if offline
                error = null,
                isLoadingMore = false,
                isOffline = isOffline,
                isUsingCache = isOffline || movies.isEmpty()
            )
        }
    )
    init {
        viewModelScope.launch {
            connectivityService.networkConnectionFlow
//                .distinctUntilChanged()
//                .debounce(800)
                .collect { networkConnection ->
                    val isOffline = (networkConnection == null)
                    state = state.copy(isOffline = isOffline, networkConnection = networkConnection)
                    if (!isOffline && state.movies.isEmpty()) {
                        loadInitialMovies()
                    }
                }
        }
    }
//    init {
//        viewModelScope.launch {
//            connectivityService.isConnected
//                .distinctUntilChanged()
////                .debounce(800)  // helps reduce iOS rapid emissions
//                .collect { connected ->
//                    state = state.copy(isOffline = !connected)
//                    if (connected && state.movies.isEmpty()) {
//                        loadInitialMovies()
//                    }
//                }
//        }
//    }
    fun loadNextMovies() {
        if (!state.isLoadingMore && !state.isEndReached && !state.isOffline &&
            state.currentPage <= state.maxPages) {
            viewModelScope.launch {
                paginator.loadNextItems()
            }
        }
    }

    private fun loadInitialMovies() {
        state = state.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val isOffline = repository.isOfflineMode()
                state = state.copy(isOffline = isOffline)
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

    // Force refresh when back online
    fun refreshWhenOnline() {
        viewModelScope.launch {
            if (!repository.isOfflineMode()) {
                paginator.reset()
                state = state.copy(movies = emptyList(), currentPage = 1)
                loadInitialMovies()
            }
        }
    }
}
