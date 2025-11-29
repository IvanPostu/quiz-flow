package com.iv127.quizflow.core.rest.api.quizresult

enum class QuizFinalizedStateType {
    ALL,
    FINALIZED_ONLY,
    NON_FINALIZED_ONLY,
}

fun String?.toQuizFinalizedStateTypeOrNull(): QuizFinalizedStateType? {
    return try {
        this?.let { QuizFinalizedStateType.valueOf(it.uppercase()) }
    } catch (e: IllegalArgumentException) {
        null
    }
}
