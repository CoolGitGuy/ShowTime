package com.example.showtime.quiz

import androidx.lifecycle.viewModelScope
import com.example.showtime.core.mvi.StoreViewModel
import com.example.showtime.movies.data.MovieRepository
import com.example.showtime.session.SessionStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuizViewModel(
    private val sessionStorage: SessionStorage,
    private val movieRepository: MovieRepository
) : StoreViewModel<QuizContract.State, QuizContract.Intent, QuizContract.Effect>(
    initialState = QuizContract.State()
) {
    private var quizQuestions: List<QuizQuestion> = emptyList()
    private var timerJob: Job? = null

    init {
        refreshCatalog()
    }

    override fun handleIntent(intent: QuizContract.Intent) {
        when (intent) {
            QuizContract.Intent.RefreshCatalog -> refreshCatalog()
            QuizContract.Intent.StartQuiz -> startQuiz()
            is QuizContract.Intent.AnswerSelected -> answerQuestion(intent.optionId)
            QuizContract.Intent.RequestAbandon -> requestAbandon()
            QuizContract.Intent.DismissAbandon -> dismissAbandon()
            QuizContract.Intent.ConfirmAbandon -> abandonQuiz()
            QuizContract.Intent.PlayAgain -> startQuiz()
        }
    }

    private fun refreshCatalog() {
        launch {
            updateState {
                it.copy(
                    isPreparingCatalog = true,
                    errorMessage = null
                )
            }
            runCatching {
                movieRepository.ensureCatalogBootstrap()
                movieRepository.countQuizCandidates()
            }.onSuccess { count ->
                updateState {
                    it.copy(
                        availableMovieCount = count,
                        canStart = count >= QUIZ_QUESTION_COUNT,
                        isPreparingCatalog = false,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                updateState {
                    it.copy(
                        isPreparingCatalog = false,
                        canStart = false,
                        errorMessage = throwable.message ?: "Quiz catalog could not be prepared."
                    )
                }
            }
        }
    }

    private fun startQuiz() {
        if (currentState.isStartingQuiz || currentState.isPreparingCatalog) {
            return
        }

        launch {
            updateState {
                it.copy(
                    isStartingQuiz = true,
                    errorMessage = null,
                    showAbandonDialog = false,
                    result = null
                )
            }

            runCatching {
                buildQuestions()
            }.onSuccess { questions ->
                quizQuestions = questions
                updateState {
                    it.copy(
                        phase = QuizPhase.Playing,
                        currentQuestion = questions.firstOrNull(),
                        questionIndex = 0,
                        correctAnswers = 0,
                        remainingSeconds = QUIZ_DURATION_SECONDS,
                        isStartingQuiz = false,
                        isSubmittingResult = false,
                        showAbandonDialog = false,
                        errorMessage = null,
                        result = null
                    )
                }
                startTimer()
            }.onFailure { throwable ->
                updateState {
                    it.copy(
                        isStartingQuiz = false,
                        errorMessage = throwable.message ?: "Quiz could not be started."
                    )
                }
            }
        }
    }

    private suspend fun buildQuestions(): List<QuizQuestion> {
        movieRepository.ensureCatalogBootstrap()
        val pool = movieRepository.getTopQuizCandidates(limit = 24)
        if (pool.size < QUIZ_QUESTION_COUNT) {
            error("Need at least 10 cached movies with posters to start the quiz.")
        }

        val preparedCandidates = mutableListOf<QuizCandidate>()
        var actorReadyCount = 0

        pool.forEachIndexed { index, movie ->
            val leadActor = if (actorReadyCount < 8 || index < 12) {
                runCatching {
                    movieRepository.getMovieCast(movie.id).firstOrNull()?.name
                }.getOrNull()
            } else {
                null
            }
            if (leadActor != null) {
                actorReadyCount += 1
            }
            preparedCandidates += QuizCandidate(
                movie = movie,
                leadActor = leadActor
            )
        }

        return buildQuizQuestions(preparedCandidates)
    }

    private fun answerQuestion(optionId: String) {
        if (currentState.phase != QuizPhase.Playing) {
            return
        }

        val question = currentState.currentQuestion ?: return
        val updatedCorrectAnswers = if (optionId == question.correctOptionId) {
            currentState.correctAnswers + 1
        } else {
            currentState.correctAnswers
        }
        val nextIndex = currentState.questionIndex + 1

        if (nextIndex >= quizQuestions.size) {
            finishQuiz(updatedCorrectAnswers, currentState.remainingSeconds)
            return
        }

        updateState {
            it.copy(
                correctAnswers = updatedCorrectAnswers,
                questionIndex = nextIndex,
                currentQuestion = quizQuestions[nextIndex],
                errorMessage = null
            )
        }
    }

    private fun requestAbandon() {
        if (currentState.phase != QuizPhase.Playing) {
            return
        }
        updateState { it.copy(showAbandonDialog = true) }
    }

    private fun dismissAbandon() {
        updateState { it.copy(showAbandonDialog = false) }
    }

    private fun abandonQuiz() {
        stopTimer()
        quizQuestions = emptyList()
        updateState {
            it.copy(
                phase = QuizPhase.Intro,
                currentQuestion = null,
                questionIndex = 0,
                correctAnswers = 0,
                remainingSeconds = QUIZ_DURATION_SECONDS,
                showAbandonDialog = false,
                isStartingQuiz = false,
                isSubmittingResult = false,
                result = null,
                errorMessage = null
            )
        }
    }

    private fun finishQuiz(
        correctAnswers: Int,
        remainingSeconds: Int
    ) {
        stopTimer()
        val score = calculateQuizScore(correctAnswers, remainingSeconds)
        val timeUsed = QUIZ_DURATION_SECONDS - remainingSeconds
        val wrongAnswers = quizQuestions.size - correctAnswers
        val userId = sessionStorage.session.value?.userId

        if (userId == null) {
            updateState {
                it.copy(
                    phase = QuizPhase.Result,
                    currentQuestion = null,
                    correctAnswers = correctAnswers,
                    remainingSeconds = remainingSeconds,
                    result = QuizResultUi(
                        score = score,
                        correctAnswers = correctAnswers,
                        wrongAnswers = wrongAnswers,
                        timeUsedSeconds = timeUsed,
                        remainingSeconds = remainingSeconds,
                        ranking = null,
                        leaderboard = emptyList()
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isSubmittingResult = true, errorMessage = null) }
            runCatching {
                val submission = movieRepository.submitQuizResult(
                    userId = userId,
                    score = score,
                    category = QUIZ_CATEGORY
                )
                val leaderboard = movieRepository.getLeaderboard(
                    category = QUIZ_CATEGORY,
                    page = 1,
                    pageSize = 10
                )
                QuizResultUi(
                    score = submission.result.score,
                    correctAnswers = correctAnswers,
                    wrongAnswers = wrongAnswers,
                    timeUsedSeconds = timeUsed,
                    remainingSeconds = remainingSeconds,
                    ranking = submission.ranking,
                    leaderboard = leaderboard
                )
            }.onSuccess { result ->
                updateState {
                    it.copy(
                        phase = QuizPhase.Result,
                        currentQuestion = null,
                        correctAnswers = correctAnswers,
                        remainingSeconds = remainingSeconds,
                        isSubmittingResult = false,
                        result = result,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                updateState {
                    it.copy(
                        phase = QuizPhase.Result,
                        currentQuestion = null,
                        correctAnswers = correctAnswers,
                        remainingSeconds = remainingSeconds,
                        isSubmittingResult = false,
                        result = QuizResultUi(
                            score = score,
                            correctAnswers = correctAnswers,
                            wrongAnswers = wrongAnswers,
                            timeUsedSeconds = timeUsed,
                            remainingSeconds = remainingSeconds,
                            ranking = null,
                            leaderboard = emptyList()
                        ),
                        errorMessage = throwable.message ?: "Quiz result could not be submitted."
                    )
                }
            }
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (currentState.phase == QuizPhase.Playing && currentState.remainingSeconds > 0) {
                delay(1_000)
                val newRemaining = currentState.remainingSeconds - 1
                updateState { it.copy(remainingSeconds = newRemaining.coerceAtLeast(0)) }
                if (newRemaining <= 0) {
                    finishQuiz(
                        correctAnswers = currentState.correctAnswers,
                        remainingSeconds = 0
                    )
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }

    companion object {
        private const val QUIZ_CATEGORY = 1
        private const val QUIZ_DURATION_SECONDS = 60
        private const val QUIZ_QUESTION_COUNT = 10
    }
}
