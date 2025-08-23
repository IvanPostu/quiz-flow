package com.iv127.quizflow.core.model.quiz.question.lang

sealed class Outcome<out T, out E : Exception> {
    data class Success<out T>(val value: T) : Outcome<T, Nothing>()
    data class Failure<out E : Exception>(val exception: E) : Outcome<Nothing, E>()

    fun asResult(): Result<T> {
        if (this is Success) {
            return Result.success(this.value)
        }
        if (this is Failure) {
            return Result.failure(this.exception)
        }
        throw IllegalStateException("""$this can't be mapped to Result type""")
    }
}
