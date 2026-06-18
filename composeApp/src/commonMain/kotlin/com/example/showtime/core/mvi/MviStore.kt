package com.example.showtime.core.mvi

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MviStore<S : UiState, E : UiEffect>(
    initialState: S
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<E>(
        replay = 0,
        extraBufferCapacity = 32
    )
    val effects: SharedFlow<E> = _effects.asSharedFlow()

    val currentState: S
        get() = _state.value

    fun setState(state: S) {
        _state.value = state
    }

    fun updateState(transform: (S) -> S) {
        _state.update(transform)
    }

    suspend fun emitEffect(effect: E) {
        _effects.emit(effect)
    }

    fun tryEmitEffect(effect: E): Boolean {
        return _effects.tryEmit(effect)
    }
}
