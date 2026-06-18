package com.example.showtime.movies.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.showtime.movies.domain.Genre
import com.example.showtime.movies.domain.MovieSortOption
import com.example.showtime.movies.domain.displayLabel
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

        FilterSummary(
            state = state,
            onIntent = onIntent
        )

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

            lazyMovies.itemCount == 0 -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No movies match the current filters.",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(onClick = { onIntent(MoviesCatalogContract.Intent.ClearFilters) }) {
                            Text("Clear filters")
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

                    when (val appendState = lazyMovies.loadState.append) {
                        is LoadState.Loading -> {
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

                        is LoadState.Error -> {
                            item {
                                Surface(
                                    tonalElevation = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(appendState.error.message ?: "More movies could not be loaded.")
                                        Button(onClick = { lazyMovies.retry() }) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSummary(
    state: MoviesCatalogContract.State,
    onIntent: (MoviesCatalogContract.Intent) -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Catalog filters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = activeFilterSummary(state),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { onIntent(MoviesCatalogContract.Intent.ToggleFilters) }
                ) {
                    Text(if (state.isFiltersExpanded) "Hide" else "Edit")
                }
            }

            if (state.isFiltersExpanded) {
                OutlinedTextField(
                    value = state.filters.query,
                    onValueChange = { onIntent(MoviesCatalogContract.Intent.QueryChanged(it)) },
                    label = { Text("Search title") },
                    modifier = Modifier.fillMaxWidth()
                )

                FilterSectionTitle("Genres")
                GenreChips(
                    genres = state.genres,
                    selectedGenreId = state.filters.genreId,
                    onGenreSelected = { genreId ->
                        onIntent(MoviesCatalogContract.Intent.GenreChanged(genreId))
                    }
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

                FilterSectionTitle("Sort")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(MovieSortOption.entries) { option ->
                        FilterChip(
                            selected = state.filters.sortOption == option,
                            onClick = { onIntent(MoviesCatalogContract.Intent.SortChanged(option)) },
                            label = { Text(option.displayLabel()) }
                        )
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
    }
}

@Composable
private fun GenreChips(
    genres: List<Genre>,
    selectedGenreId: String?,
    onGenreSelected: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedGenreId == null,
                onClick = { onGenreSelected(null) },
                label = { Text("All") }
            )
        }
        items(genres) { genre ->
            FilterChip(
                selected = selectedGenreId == genre.id,
                onClick = {
                    onGenreSelected(
                        if (selectedGenreId == genre.id) {
                            null
                        } else {
                            genre.id
                        }
                    )
                },
                label = { Text(genre.name) }
            )
        }
    }
}

@Composable
private fun FilterSectionTitle(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
}

private fun activeFilterSummary(
    state: MoviesCatalogContract.State
): String {
    val parts = buildList {
        if (state.filters.query.isNotBlank()) {
            add("query: ${state.filters.query}")
        }
        state.genres.firstOrNull { it.id == state.filters.genreId }?.let {
            add("genre: ${it.name}")
        }
        state.filters.minYear?.let { add("from: $it") }
        state.filters.maxYear?.let { add("to: $it") }
        state.filters.minRating?.let { add("rating >= $it") }
        add("sort: ${state.filters.sortOption.displayLabel()}")
    }
    return if (parts.isEmpty()) {
        "No active filters."
    } else {
        parts.joinToString(" | ")
    }
}
