package com.example.showtime.core.ui

sealed interface AsyncData<out T> {
    data object Uninitialized : AsyncData<Nothing>

    data object Loading : AsyncData<Nothing>

    data object Empty : AsyncData<Nothing>

    data class Data<T>(
        val value: T,
        val isRefreshing: Boolean = false,
        val isOffline: Boolean = false
    ) : AsyncData<T>

    data class Error(
        val message: String? = null,
        val cause: Throwable? = null,
        val isOffline: Boolean = false
    ) : AsyncData<Nothing>
}
