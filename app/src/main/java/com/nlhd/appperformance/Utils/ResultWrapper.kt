package com.nlhd.appperformance.Utils

sealed class ResultWrapper<out T> {
    data class Success<T>(val value: T): ResultWrapper<T>()
    data class Error(val exception: Exception): ResultWrapper<Nothing>()
}

