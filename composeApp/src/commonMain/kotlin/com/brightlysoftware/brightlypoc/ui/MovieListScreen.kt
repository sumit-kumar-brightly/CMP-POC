package com.brightlysoftware.brightlypoc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import com.brightlysoftware.brightlypoc.viewmodel.MovieListViewModel
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    viewModel: MovieListViewModel = koinInject()
) {
    val state = viewModel.state
    val listState = rememberLazyListState()
    // Handle pagination when user scrolls to the end
    LaunchedEffect(listState, state.movies.size) {  // Add state.movies.size as dependency
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

            println("DEBUG SCROLL: Last visible: $lastVisibleItemIndex, Total items: $totalItemsCount")

            // Return only scroll-related data
            Pair(lastVisibleItemIndex, totalItemsCount)
        }
            .collect { (lastVisibleIndex, totalItems) ->
                // Use current state value directly, not captured value
                val currentMoviesCount = state.movies.size

                println("DEBUG TRIGGER CHECK: lastVisible=$lastVisibleIndex, totalItems=$totalItems, moviesCount=$currentMoviesCount")
                println("DEBUG CONDITIONS: isLoadingMore=${state.isLoadingMore}, isEndReached=${state.isEndReached}, error=${state.error}")
                // Check if we should load more
                val shouldLoadMore = lastVisibleIndex >= totalItems - 3 &&
                        totalItems > 0 &&
                        !state.isLoadingMore &&
                        !state.isEndReached &&
                        state.error == null &&
                        currentMoviesCount > 0

                if (shouldLoadMore) {
                    println("DEBUG: PAGINATION TRIGGERED! Loading next page...")
                    viewModel.loadNextMovies()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Popular Movies") },
                // NEW: Show offline indicator
                actions = {
                    NetworkBadge(isOffline = state.isOffline)
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // NEW: Offline banner
            if (state.isOffline && state.movies.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "You're offline. Showing cached movies.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    // ... existing states ...
                    state.isLoading && state.movies.isEmpty() -> {
                        LoadingIndicator()
                    }

                    state.error != null && state.movies.isEmpty() -> {
                        ErrorState(
                            error = if (state.isOffline) {
                                "No internet connection and no cached data available"
                            } else {
                                state.error
                            },
                            onRetry = viewModel::retry
                        )
                    }

                    else -> {
                        MovieList(
                            movies = state.movies,
                            isLoadingMore = state.isLoadingMore,
                            listState = listState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieList(
    movies: List<com.brightlysoftware.brightlypoc.models.Movie>,
    isLoadingMore: Boolean,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = movies,
            key = { movie -> "${movie.id}_${movies.indexOf(movie)}" }
        ) { movie ->
            MovieItem(movie = movie)
        }

        // Loading more indicator
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun NetworkBadge(isOffline: Boolean) {
    val icon = if (isOffline) Icons.Default.CloudOff else Icons.Default.CloudDone
    val tint = if (isOffline) Color.Red else Color.Green
    Icon(
        imageVector = icon,
        contentDescription = if (isOffline) "Offline" else "Online",
        tint = tint,
        modifier = Modifier
            .padding(end = 16.dp)
            .size(20.dp)
    )
}
@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading popular movies...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onRetry
            ) {
                Text("Retry")
            }
        }
    }
}