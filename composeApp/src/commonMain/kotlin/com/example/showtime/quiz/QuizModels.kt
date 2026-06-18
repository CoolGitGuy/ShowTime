package com.example.showtime.quiz

import com.example.showtime.movies.domain.LeaderboardEntry
import com.example.showtime.movies.domain.MovieSummary

data class QuizCandidate(
    val movie: MovieSummary,
    val leadActor: String? = null
)

enum class QuizQuestionType {
    GuessMovie,
    GuessYear,
    GuessLeadActor
}

data class QuizOption(
    val id: String,
    val label: String
)

data class QuizQuestion(
    val id: Int,
    val type: QuizQuestionType,
    val prompt: String,
    val supportingText: String,
    val imageUrl: String?,
    val options: List<QuizOption>,
    val correctOptionId: String
)

enum class QuizPhase {
    Intro,
    Playing,
    Result
}

data class QuizResultUi(
    val score: Float,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val timeUsedSeconds: Int,
    val remainingSeconds: Int,
    val ranking: Int?,
    val leaderboard: List<LeaderboardEntry>
)
