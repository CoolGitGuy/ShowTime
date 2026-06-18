package com.example.showtime.profile

import com.example.showtime.auth.AuthRepository
import com.example.showtime.core.mvi.StoreViewModel
import com.example.showtime.movies.data.MovieRepository
import com.example.showtime.session.SessionStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val sessionStorage: SessionStorage,
    private val authRepository: AuthRepository,
    private val movieRepository: MovieRepository
) : StoreViewModel<ProfileContract.State, ProfileContract.Intent, ProfileContract.Effect>(
    initialState = ProfileContract.State()
) {
    init {
        observeProfileState()
        refreshOnSessionChange()
    }

    override fun handleIntent(intent: ProfileContract.Intent) {
        when (intent) {
            ProfileContract.Intent.Logout -> logout()
            ProfileContract.Intent.Refresh -> refresh()
        }
    }

    private fun observeProfileState() {
        launch {
            sessionStorage.observeSession()
                .flatMapLatest { session ->
                    if (session == null) {
                        flowOf(ProfileSnapshot())
                    } else {
                        combine(
                            movieRepository.observeFavoriteCount(session.userId),
                            movieRepository.observeWatchlistCount(session.userId),
                            movieRepository.observeBestQuizScore(session.userId),
                            movieRepository.observeQuizPlayCount(session.userId),
                            movieRepository.observeRecentQuizResults(session.userId, limit = 5)
                        ) { favoriteCount, watchlistCount, bestQuizScore, quizPlayCount, recentResults ->
                            ProfileSnapshot(
                                session = session,
                                favoriteCount = favoriteCount,
                                watchlistCount = watchlistCount,
                                bestQuizScore = bestQuizScore,
                                quizPlayCount = quizPlayCount,
                                recentResults = recentResults
                            )
                        }
                    }
                }
                .collectLatest { snapshot ->
                    updateState { state ->
                        state.copy(
                            session = snapshot.session,
                            favoriteCount = snapshot.favoriteCount,
                            watchlistCount = snapshot.watchlistCount,
                            bestQuizScore = snapshot.bestQuizScore,
                            quizPlayCount = snapshot.quizPlayCount,
                            recentResults = snapshot.recentResults,
                            errorMessage = if (snapshot.session == null) null else state.errorMessage
                        )
                    }
                }
        }
    }

    private fun refreshOnSessionChange() {
        launch {
            sessionStorage.observeSession().collectLatest { session ->
                if (session != null) {
                    refresh()
                } else {
                    updateState { ProfileContract.State() }
                }
            }
        }
    }

    private fun refresh() {
        val session = sessionStorage.session.value ?: return
        launch {
            updateState { state ->
                state.copy(
                    isRefreshing = true,
                    errorMessage = null
                )
            }
            runCatching {
                coroutineScope {
                    val refreshProfile = async { authRepository.refreshProfile() }
                    val refreshFavorites = async { movieRepository.refreshFavorites(session.userId) }
                    val refreshWatchlist = async { movieRepository.refreshWatchlist(session.userId) }
                    val refreshQuizResults = async { movieRepository.refreshQuizResults(session.userId) }
                    refreshProfile.await()
                    refreshFavorites.await()
                    refreshWatchlist.await()
                    refreshQuizResults.await()
                }
            }.onFailure { throwable ->
                updateState { state ->
                    state.copy(
                        isRefreshing = false,
                        errorMessage = throwable.message ?: "Profile refresh failed."
                    )
                }
            }.onSuccess {
                updateState { state ->
                    state.copy(
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun logout() {
        launch {
            updateState { state ->
                state.copy(
                    isLoggingOut = true,
                    errorMessage = null
                )
            }
            runCatching { authRepository.logout() }
                .onFailure { throwable ->
                    updateState { state ->
                        state.copy(
                            isLoggingOut = false,
                            errorMessage = throwable.message ?: "Logout failed."
                        )
                    }
                }
        }
    }
}

private data class ProfileSnapshot(
    val session: com.example.showtime.session.UserSession? = null,
    val favoriteCount: Int = 0,
    val watchlistCount: Int = 0,
    val bestQuizScore: Float? = null,
    val quizPlayCount: Int = 0,
    val recentResults: List<com.example.showtime.movies.domain.QuizHistoryEntry> = emptyList()
)
