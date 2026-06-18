package com.example.showtime.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.showtime.movies.ui.MovieCard

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onMovieClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Favorites", style = MaterialTheme.typography.headlineMedium)
        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.movies) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie.id) },
                    action = {
                        Button(
                            onClick = { viewModel.onIntent(FavoritesContract.Intent.Remove(movie.id)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Remove")
                        }
                    }
                )
            }
        }
    }
}
