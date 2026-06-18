package com.example.showtime.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class StoreViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {
    private val store = MviStore<S, E>(initialState = initialState)

    val state: StateFlow<S> = store.state
    val effects: SharedFlow<E> = store.effects

    protected val currentState: S
        get() = store.currentState

    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    protected abstract fun handleIntent(intent: I)

    protected fun setState(state: S) {
        store.setState(state)
    }

    protected fun updateState(transform: (S) -> S) {
        store.updateState(transform)
    }

    protected suspend fun emitEffect(effect: E) {
        store.emitEffect(effect)
    }

    protected fun tryEmitEffect(effect: E): Boolean {
        return store.tryEmitEffect(effect)
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(block = block)
    }
}
