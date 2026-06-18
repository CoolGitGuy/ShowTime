package com.example.showtime.quiz

import com.example.showtime.movies.domain.MovieSummary
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuizEngineTest {
    @Test
    fun `score is capped at hundred`() {
        val score = calculateQuizScore(
            correctAnswers = 10,
            remainingSeconds = 60
        )

        assertEquals(100f, score)
    }

    @Test
    fun `engine builds ten questions with four options each`() {
        val questions = buildQuizQuestions(
            candidates = buildCandidates(),
            random = Random(7)
        )

        assertEquals(10, questions.size)
        assertTrue(questions.all { question -> question.options.size == 4 })
        assertTrue(questions.all { question ->
            question.options.any { option -> option.id == question.correctOptionId }
        })
    }

    @Test
    fun `engine keeps recommended distribution when data is available`() {
        val types = buildQuizQuestions(
            candidates = buildCandidates(),
            random = Random(13)
        ).groupingBy { it.type }.eachCount()

        assertEquals(4, types[QuizQuestionType.GuessMovie])
        assertEquals(3, types[QuizQuestionType.GuessYear])
        assertEquals(3, types[QuizQuestionType.GuessLeadActor])
    }

    private fun buildCandidates(): List<QuizCandidate> {
        return (1..12).map { index ->
            QuizCandidate(
                movie = MovieSummary(
                    id = "tt$index",
                    title = "Movie $index",
                    year = 2000 + index,
                    imdbRating = 7.0f + (index / 10f),
                    imdbVotes = 1000 + index,
                    posterUrl = "https://example.com/$index.jpg",
                    backdropUrl = "https://example.com/$index-bg.jpg",
                    genres = listOf("Drama")
                ),
                leadActor = "Actor $index"
            )
        }
    }
}
