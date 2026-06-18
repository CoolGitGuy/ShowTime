@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@file:Suppress("DEPRECATION")

package com.example.showtime.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun QuizScreen(
    viewModel: QuizViewModel
) {
    val state by viewModel.state.collectAsState()

    QuizScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@Composable
private fun QuizScreen(
    state: QuizContract.State,
    onIntent: (QuizContract.Intent) -> Unit
) {
    if (state.phase == QuizPhase.Playing) {
        BackHandler {
            onIntent(QuizContract.Intent.RequestAbandon)
        }
    }

    when (state.phase) {
        QuizPhase.Intro -> QuizIntroContent(state = state, onIntent = onIntent)
        QuizPhase.Playing -> QuizPlayingContent(state = state, onIntent = onIntent)
        QuizPhase.Result -> QuizResultContent(state = state, onIntent = onIntent)
    }

    if (state.showAbandonDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(QuizContract.Intent.DismissAbandon) },
            title = { Text("Abandon quiz?") },
            text = { Text("Current progress will be lost and this run will not be submitted.") },
            confirmButton = {
                Button(onClick = { onIntent(QuizContract.Intent.ConfirmAbandon) }) {
                    Text("Abandon")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onIntent(QuizContract.Intent.DismissAbandon) }) {
                    Text("Continue")
                }
            }
        )
    }
}

@Composable
private fun QuizIntroContent(
    state: QuizContract.State,
    onIntent: (QuizContract.Intent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quiz",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "You get 10 movie questions and 60 seconds. No backtracking once the run starts.",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Scoring: correct answers x (9 + remainingSeconds / 60), capped at 100.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Local catalog status",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Cached quiz-ready movies: ${state.availableMovieCount}")
                Text(
                    text = if (state.canStart) {
                        "Catalog is ready for the quiz."
                    } else {
                        "Need at least 10 cached movies with an image before the quiz can start."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (state.isPreparingCatalog || state.isStartingQuiz) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
                Text(
                    text = if (state.isStartingQuiz) {
                        "Preparing questions..."
                    } else {
                        "Syncing catalog..."
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onIntent(QuizContract.Intent.StartQuiz) },
                enabled = state.canStart && !state.isPreparingCatalog && !state.isStartingQuiz,
                modifier = Modifier.weight(1f)
            ) {
                Text("Start quiz")
            }
            OutlinedButton(
                onClick = { onIntent(QuizContract.Intent.RefreshCatalog) },
                enabled = !state.isPreparingCatalog && !state.isStartingQuiz,
                modifier = Modifier.weight(1f)
            ) {
                Text("Refresh catalog")
            }
        }
    }
}

@Composable
private fun QuizPlayingContent(
    state: QuizContract.State,
    onIntent: (QuizContract.Intent) -> Unit
) {
    val question = state.currentQuestion
    if (question == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val progress = (state.questionIndex + 1) / state.totalQuestions.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Question ${state.questionIndex + 1}/${state.totalQuestions}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${state.remainingSeconds}s",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        item {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text(
                text = question.prompt,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Text(
                text = question.supportingText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        question.imageUrl?.let { imageUrl ->
            item {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = question.prompt,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        items(question.options) { option ->
            Button(
                onClick = { onIntent(QuizContract.Intent.AnswerSelected(option.id)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(option.label)
            }
        }
    }
}

@Composable
private fun QuizResultContent(
    state: QuizContract.State,
    onIntent: (QuizContract.Intent) -> Unit
) {
    val result = state.result

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Quiz Result",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        result?.let { summary ->
            item {
                Surface(
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Score ${summary.score.formatScore()}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text("Correct: ${summary.correctAnswers}")
                        Text("Wrong: ${summary.wrongAnswers}")
                        Text("Time used: ${summary.timeUsedSeconds}s")
                        Text("Time left: ${summary.remainingSeconds}s")
                        summary.ranking?.let { ranking ->
                            Text("Leaderboard rank: #$ranking")
                        }
                    }
                }
            }
            if (summary.leaderboard.isNotEmpty()) {
                item {
                    Text(
                        text = "Leaderboard",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                items(summary.leaderboard) { entry ->
                    Surface(
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "#${entry.rank} ${entry.fullName}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "@${entry.username}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(entry.score.formatScore())
                        }
                    }
                }
            }
        }
        state.errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        if (state.isSubmittingResult) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Text("Submitting result...")
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onIntent(QuizContract.Intent.PlayAgain) },
                    enabled = !state.isStartingQuiz && !state.isSubmittingResult,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Play again")
                }
                OutlinedButton(
                    onClick = { onIntent(QuizContract.Intent.RefreshCatalog) },
                    enabled = !state.isPreparingCatalog && !state.isSubmittingResult,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Refresh catalog")
                }
            }
        }
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
