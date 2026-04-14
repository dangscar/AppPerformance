package com.nlhd.appperformance.Utils

sealed class ResultUI<out T> {
    data class Success<T>(val data: T): ResultUI<T>()
    data class Error<String>(val message: String): ResultUI<Nothing>()
    object Loading: ResultUI<Nothing>()
    object Idle: ResultUI<Nothing>()
}

