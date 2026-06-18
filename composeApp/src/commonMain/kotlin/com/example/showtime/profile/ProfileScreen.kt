package com.example.showtime.profile

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.showtime.movies.domain.QuizHistoryEntry
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {
    val state by viewModel.state.collectAsState()

    ProfileScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
private fun ProfileScreen(
    state: ProfileContract.State,
    onIntent: (ProfileContract.Intent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        state.session?.let { session ->
            item {
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = session.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "@${session.username}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "User ID: ${session.userId}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } ?: item {
            Text(
                text = "No active user session.",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Favorites",
                    value = state.favoriteCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Watchlist",
                    value = state.watchlistCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Best Score",
                    value = state.bestQuizScore?.formatScore() ?: "-",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Quiz Plays",
                    value = state.quizPlayCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.recentResults.isNotEmpty()) {
            item {
                Text(
                    text = "Recent quiz results",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(state.recentResults) { result ->
                QuizHistoryCard(result = result)
            }
        }

        state.errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (state.isRefreshing || state.isLoggingOut) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = if (state.isLoggingOut) {
                            "Logging out..."
                        } else {
                            "Refreshing profile..."
                        }
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { onIntent(ProfileContract.Intent.Refresh) },
                enabled = !state.isRefreshing && !state.isLoggingOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh profile")
            }
        }

        item {
            Button(
                onClick = { onIntent(ProfileContract.Intent.Logout) },
                enabled = !state.isRefreshing && !state.isLoggingOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun QuizHistoryCard(
    result: QuizHistoryEntry
) {
    Surface(
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Score ${result.score.formatScore()}",
                fontWeight = FontWeight.SemiBold
            )
            Text("Played: ${formatPlayedAt(result.playedAt)}")
            result.ranking?.let { ranking ->
                Text("Rank: #$ranking")
            }
        }
    }
}

private fun formatPlayedAt(value: Long): String {
    val epochMillis = if (value < 1_000_000_000_000L) value * 1_000 else value
    val dateTime = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return buildString {
        append(dateTime.date)
        append(" ")
        append(dateTime.hour.toString().padStart(2, '0'))
        append(":")
        append(dateTime.minute.toString().padStart(2, '0'))
    }
}

private fun Float.formatScore(): String {
    val rounded = (this * 100).toInt() / 100.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}
