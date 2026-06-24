package com.habizy.app.ui.viewmodel

/**
 * Generic sealed class representing the state of a UI-bound asynchronous operation.
 */
sealed class UiState<out T> {

    /** Data is being loaded. */
    data object Loading : UiState<Nothing>()

    /** Operation succeeded with [data]. */
    data class Success<T>(val data: T) : UiState<T>()

    /** Operation failed with an error [message]. */
    data class Error(val message: String) : UiState<Nothing>()

    /** The result set is intentionally empty (e.g. no items yet). */
    data object Empty : UiState<Nothing>()

    /** Convenience helpers. */
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isEmpty: Boolean get() = this is Empty

    /** Returns the data if this is [Success], otherwise null. */
    fun dataOrNull(): T? = (this as? Success)?.data

    /** Returns the error message if this is [Error], otherwise null. */
    fun errorOrNull(): String? = (this as? Error)?.message
}
