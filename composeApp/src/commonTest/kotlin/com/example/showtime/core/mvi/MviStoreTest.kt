package com.example.showtime.core.mvi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MviStoreTest {
    @Test
    fun stateUpdatesDeterministically() {
        val store = MviStore<CounterState, CounterEffect>(initialState = CounterState())

        store.updateState { state -> state.copy(count = state.count + 1) }
        store.updateState { state -> state.copy(count = state.count + 1) }

        assertEquals(2, store.currentState.count)
    }

    @Test
    fun effectsAreOneShot() {
        val store = MviStore<CounterState, CounterEffect>(initialState = CounterState())

        assertTrue(store.tryEmitEffect(CounterEffect.NavigateToNext))
        assertTrue(store.effects.replayCache.isEmpty())
    }
}

private data class CounterState(
    val count: Int = 0
) : UiState

private sealed interface CounterEffect : UiEffect {
    data object NavigateToNext : CounterEffect
}
