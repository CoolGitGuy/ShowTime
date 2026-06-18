package com.example.showtime.movies.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.showtime.core.ui.AsyncData

@Composable
fun MovieDetailsScreen(
    viewModel: MovieDetailsViewModel
) {
    val state by viewModel.state.collectAsState()

    MovieDetailsScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
private fun MovieDetailsScreen(
    state: MovieDetailsContract.State,
    onIntent: (MovieDetailsContract.Intent) -> Unit
) {
    when (val movieState = state.movie) {
        is AsyncData.Data -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AsyncImage(
                        model = movieState.value.backdropUrl ?: movieState.value.posterUrl,
                        contentDescription = movieState.value.title,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = movieState.value.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                item {
                    Text(
                        text = buildString {
                            append(movieState.value.year ?: "Unknown year")
                            movieState.value.runtime?.let { append("  •  ").append(it).append(" min") }
                            movieState.value.imdbRating?.let { append("  •  IMDb ").append(it) }
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                item {
                    Text(
                        text = movieState.value.genres.joinToString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    Button(
                        onClick = { onIntent(MovieDetailsContract.Intent.ToggleFavorite) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.isFavorite) "Remove Favorite" else "Add Favorite")
                    }
                }
                item {
                    Button(
                        onClick = { onIntent(MovieDetailsContract.Intent.ToggleWatchlist) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.isWatchlist) "Remove Watchlist" else "Add Watchlist")
                    }
                }
                item {
                    Text(
                        text = movieState.value.overview.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                item {
                    Text(
                        text = "Cast",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                items(movieState.value.cast) { castMember ->
                    Text(
                        text = castMember.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        is AsyncData.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(movieState.message ?: "Movie details failed to load.")
                Button(onClick = { onIntent(MovieDetailsContract.Intent.Refresh) }) {
                    Text("Retry")
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}
