package com.example.showtime.quiz

import com.example.showtime.core.mvi.UiEffect
import com.example.showtime.core.mvi.UiIntent
import com.example.showtime.core.mvi.UiState

object QuizContract {
    data class State(
        val phase: QuizPhase = QuizPhase.Intro,
        val currentQuestion: QuizQuestion? = null,
        val questionIndex: Int = 0,
        val totalQuestions: Int = 10,
        val correctAnswers: Int = 0,
        val remainingSeconds: Int = 60,
        val availableMovieCount: Int = 0,
        val canStart: Boolean = false,
        val isPreparingCatalog: Boolean = true,
        val isStartingQuiz: Boolean = false,
        val isSubmittingResult: Boolean = false,
        val showAbandonDialog: Boolean = false,
        val errorMessage: String? = null,
        val result: QuizResultUi? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        data object RefreshCatalog : Intent
        data object StartQuiz : Intent
        data class AnswerSelected(val optionId: String) : Intent
        data object RequestAbandon : Intent
        data object DismissAbandon : Intent
        data object ConfirmAbandon : Intent
        data object PlayAgain : Intent
    }

    sealed interface Effect : UiEffect
}
