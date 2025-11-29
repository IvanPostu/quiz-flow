package com.iv127.quizflow.core.rest.api.quizresult

import com.iv127.quizflow.core.rest.api.SortOrder

interface QuizResultsRoutes {
    companion object {
        const val ROUTE_PATH: String = "/quiz-results"
    }

    suspend fun get(accessToken: String, quizId: String): QuizResultResponse

    suspend fun list(
        accessToken: String,
        offset: Int?,
        limit: Int?,
        sortOrder: SortOrder?,
        quizFinalizedStateType: QuizFinalizedStateType?,
    ): List<QuizResultResponse>

}

