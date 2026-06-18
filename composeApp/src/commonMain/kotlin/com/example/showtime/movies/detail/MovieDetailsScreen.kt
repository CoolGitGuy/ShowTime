package com.example.showtime.movies.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
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
    val uriHandler = LocalUriHandler.current

    when (val movieState = state.movie) {
        is AsyncData.Data -> {
            val movie = movieState.value
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AsyncImage(
                        model = movie.backdropUrl ?: movie.posterUrl,
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                item {
                    Text(
                        text = buildString {
                            append(movie.year ?: "Unknown year")
                            movie.runtime?.let { append(" | ").append(it).append(" min") }
                            movie.imdbRating?.let { append(" | IMDb ").append(it) }
                            movie.imdbVotes?.let { append(" | Votes ").append(it) }
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (movie.genres.isNotEmpty()) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(movie.genres) { genre ->
                                Surface(
                                    tonalElevation = 2.dp
                                ) {
                                    Text(
                                        text = genre,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onIntent(MovieDetailsContract.Intent.ToggleFavorite) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (state.isFavorite) "Remove favorite" else "Add favorite")
                        }
                        Button(
                            onClick = { onIntent(MovieDetailsContract.Intent.ToggleWatchlist) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (state.isWatchlist) "Remove watchlist" else "Add watchlist")
                        }
                    }
                }
                movie.tagline?.takeIf { it.isNotBlank() }?.let { tagline ->
                    item {
                        Text(
                            text = tagline,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                movie.overview?.takeIf { it.isNotBlank() }?.let { overview ->
                    item {
                        InfoBlock(
                            title = "Overview",
                            value = overview
                        )
                    }
                }
                movie.releaseDate?.takeIf { it.isNotBlank() }?.let { releaseDate ->
                    item {
                        InfoBlock(
                            title = "Release date",
                            value = releaseDate
                        )
                    }
                }
                movie.homepage?.takeIf { it.isNotBlank() }?.let { homepage ->
                    item {
                        InfoBlock(
                            title = "Homepage",
                            value = homepage,
                            modifier = Modifier.clickable { uriHandler.openUri(homepage) }
                        )
                    }
                }
                item {
                    Text(
                        text = "Cast",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (movie.cast.isEmpty()) {
                    item {
                        Text(
                            text = "Cast information is not available yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(movie.cast) { castMember ->
                        Surface(
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = castMember.name,
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
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

@Composable
private fun InfoBlock(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
