package com.example.showtime.quiz

import kotlin.math.min
import kotlin.random.Random

private const val DEFAULT_QUESTION_COUNT = 10

fun calculateQuizScore(
    correctAnswers: Int,
    remainingSeconds: Int
): Float {
    val clampedCorrect = correctAnswers.coerceAtLeast(0)
    val clampedRemaining = remainingSeconds.coerceIn(0, 60)
    val rawScore = clampedCorrect * (9f + (clampedRemaining / 60f))
    return min(rawScore, 100f)
}

fun buildQuizQuestions(
    candidates: List<QuizCandidate>,
    random: Random = Random.Default,
    questionCount: Int = DEFAULT_QUESTION_COUNT
): List<QuizQuestion> {
    require(candidates.size >= questionCount) {
        "At least $questionCount quiz candidates are required."
    }

    val shuffledCandidates = candidates.shuffled(random)
    val questionPlan = buildList {
        repeat(4) { add(QuizQuestionType.GuessMovie) }
        repeat(3) { add(QuizQuestionType.GuessYear) }
        repeat(3) { add(QuizQuestionType.GuessLeadActor) }
    }.shuffled(random)

    val questions = mutableListOf<QuizQuestion>()
    val baseCandidates = shuffledCandidates.take(questionCount)

    baseCandidates.forEachIndexed { index, candidate ->
        val preferredType = questionPlan.getOrNull(index) ?: QuizQuestionType.GuessMovie
        val question = buildQuestionWithFallback(
            candidate = candidate,
            pool = shuffledCandidates,
            preferredType = preferredType,
            questionId = index + 1,
            random = random
        ) ?: error("Unable to build quiz question for ${candidate.movie.id}.")
        questions += question
    }

    return questions
}

private fun buildQuestionWithFallback(
    candidate: QuizCandidate,
    pool: List<QuizCandidate>,
    preferredType: QuizQuestionType,
    questionId: Int,
    random: Random
): QuizQuestion? {
    val orderedTypes = listOf(preferredType) + QuizQuestionType.entries.filterNot { it == preferredType }
    return orderedTypes.firstNotNullOfOrNull { type ->
        when (type) {
            QuizQuestionType.GuessMovie -> buildGuessMovieQuestion(candidate, pool, questionId, random)
            QuizQuestionType.GuessYear -> buildGuessYearQuestion(candidate, pool, questionId, random)
            QuizQuestionType.GuessLeadActor -> buildGuessLeadActorQuestion(candidate, pool, questionId, random)
        }
    }
}

private fun buildGuessMovieQuestion(
    candidate: QuizCandidate,
    pool: List<QuizCandidate>,
    questionId: Int,
    random: Random
): QuizQuestion? {
    val distractors = pool
        .filter { it.movie.id != candidate.movie.id }
        .distinctBy { it.movie.title }
        .shuffled(random)
        .take(3)

    if (distractors.size < 3) {
        return null
    }

    val options = (distractors.map { it.movie.title } + candidate.movie.title)
        .shuffled(random)
        .map { title -> QuizOption(id = title, label = title) }

    return QuizQuestion(
        id = questionId,
        type = QuizQuestionType.GuessMovie,
        prompt = "Guess the movie",
        supportingText = "Pick the correct title for the poster.",
        imageUrl = candidate.movie.posterUrl ?: candidate.movie.backdropUrl,
        options = options,
        correctOptionId = candidate.movie.title
    )
}

private fun buildGuessYearQuestion(
    candidate: QuizCandidate,
    pool: List<QuizCandidate>,
    questionId: Int,
    random: Random
): QuizQuestion? {
    val correctYear = candidate.movie.year ?: return null
    val distractors = pool
        .filter { it.movie.id != candidate.movie.id }
        .mapNotNull { it.movie.year }
        .distinct()
        .filter { it != correctYear }
        .shuffled(random)
        .take(3)

    if (distractors.size < 3) {
        return null
    }

    val options = (distractors + correctYear)
        .shuffled(random)
        .map { year -> QuizOption(id = year.toString(), label = year.toString()) }

    return QuizQuestion(
        id = questionId,
        type = QuizQuestionType.GuessYear,
        prompt = "Guess the movie year",
        supportingText = candidate.movie.title,
        imageUrl = candidate.movie.posterUrl ?: candidate.movie.backdropUrl,
        options = options,
        correctOptionId = correctYear.toString()
    )
}

private fun buildGuessLeadActorQuestion(
    candidate: QuizCandidate,
    pool: List<QuizCandidate>,
    questionId: Int,
    random: Random
): QuizQuestion? {
    val correctActor = candidate.leadActor ?: return null
    val distractors = pool
        .filter { it.movie.id != candidate.movie.id }
        .mapNotNull { it.leadActor }
        .distinct()
        .filter { it != correctActor }
        .shuffled(random)
        .take(3)

    if (distractors.size < 3) {
        return null
    }

    val options = (distractors + correctActor)
        .shuffled(random)
        .map { actor -> QuizOption(id = actor, label = actor) }

    return QuizQuestion(
        id = questionId,
        type = QuizQuestionType.GuessLeadActor,
        prompt = "Guess the lead actor",
        supportingText = candidate.movie.title,
        imageUrl = candidate.movie.backdropUrl ?: candidate.movie.posterUrl,
        options = options,
        correctOptionId = correctActor
    )
}
