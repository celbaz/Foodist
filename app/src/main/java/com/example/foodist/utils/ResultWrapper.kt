package com.example.foodist.utils

import retrofit2.HttpException
import java.io.IOException

sealed class ResultWrapper<out T> {
  data class Success<out T>(val value: T) : ResultWrapper<T>()
  data class GenericError(val code: Int? = null, val error: HttpException? = null) : ResultWrapper<Nothing>()
  object NetworkError : ResultWrapper<Nothing>()
}

fun handleThrowable(throwable: Throwable): ResultWrapper<Nothing> {
  return when (throwable) {
    is IOException -> ResultWrapper.NetworkError
    is HttpException -> {
      val code = throwable.code()
      ResultWrapper.GenericError(code)
    }
    else -> {
      ResultWrapper.GenericError(null, null)
    }
  }
}