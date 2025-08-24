package com.iv127.quizflow.core.model.quiz.question.lang

sealed class Outcome<out T, out E : Exception> {
    data class Success<out T>(val value: T) : Outcome<T, Nothing>()
    data class Failure<out E : Exception>(val exception: E) : Outcome<Nothing, E>()

    fun asResult(): Result<T> {
        return when (this) {
            is Success -> {
                Result.success(this.value)
            }

            is Failure -> {
                Result.failure(this.exception)
            }
        }
    }
}
