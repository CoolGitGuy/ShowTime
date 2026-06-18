package com.example.showtime.movies.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.showtime.movies.domain.MovieSortOption
import com.example.showtime.movies.ui.MovieCard

@Composable
fun MoviesCatalogScreen(
    viewModel: MoviesCatalogViewModel,
    onMovieClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    MoviesCatalogScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onMovieClick = onMovieClick
    )
}

@Composable
private fun MoviesCatalogScreen(
    state: MoviesCatalogContract.State,
    onIntent: (MoviesCatalogContract.Intent) -> Unit,
    onMovieClick: (String) -> Unit
) {
    val lazyMovies = state.pagedMovies.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Movies",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = { onIntent(MoviesCatalogContract.Intent.ToggleFilters) }
        ) {
            Text(if (state.isFiltersExpanded) "Hide Filters" else "Show Filters")
        }

        if (state.isFiltersExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.filters.query,
                    onValueChange = { onIntent(MoviesCatalogContract.Intent.QueryChanged(it)) },
                    label = { Text("Search title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.filters.genreId.orEmpty(),
                    onValueChange = {
                        onIntent(
                            MoviesCatalogContract.Intent.GenreChanged(
                                genreId = it.ifBlank { null }
                            )
                        )
                    },
                    label = { Text("Genre id") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.filters.minYear?.toString().orEmpty(),
                        onValueChange = { onIntent(MoviesCatalogContract.Intent.MinYearChanged(it)) },
                        label = { Text("Min year") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.filters.maxYear?.toString().orEmpty(),
                        onValueChange = { onIntent(MoviesCatalogContract.Intent.MaxYearChanged(it)) },
                        label = { Text("Max year") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = state.filters.minRating?.toString().orEmpty(),
                    onValueChange = { onIntent(MoviesCatalogContract.Intent.MinRatingChanged(it)) },
                    label = { Text("Min rating") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Sort",
                    style = MaterialTheme.typography.titleSmall
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(MovieSortOption.entries) { option ->
                        Button(
                            onClick = { onIntent(MoviesCatalogContract.Intent.SortChanged(option)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(option.name)
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onIntent(MoviesCatalogContract.Intent.ApplyFilters) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply")
                    }

                    Button(
                        onClick = { onIntent(MoviesCatalogContract.Intent.ClearFilters) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear")
                    }
                }
            }
        }

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        when {
            lazyMovies.loadState.refresh is LoadState.Loading && lazyMovies.itemCount == 0 -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            lazyMovies.loadState.refresh is LoadState.Error && lazyMovies.itemCount == 0 -> {
                val error = (lazyMovies.loadState.refresh as LoadState.Error).error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(error.message ?: "Catalog failed to load.")
                        Button(onClick = { lazyMovies.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        count = lazyMovies.itemCount,
                        key = lazyMovies.itemKey { it.id }
                    ) { index ->
                        val movie = lazyMovies[index] ?: return@items
                        MovieCard(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) }
                        )
                    }

                    if (lazyMovies.loadState.append is LoadState.Loading) {
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
        }
    }
}
