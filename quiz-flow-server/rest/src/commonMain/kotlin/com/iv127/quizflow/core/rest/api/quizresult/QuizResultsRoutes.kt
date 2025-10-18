package com.iv127.quizflow.core.rest.api.quizresult

import com.iv127.quizflow.core.rest.api.SortOrder
import com.iv127.quizflow.core.rest.api.authorization.ApiAuthorization

interface QuizResultsRoutes {
    companion object {
        const val ROUTE_PATH: String = "/quiz-results"
    }

    suspend fun get(accessToken: String, quizId: String): QuizResultResponse

    suspend fun list(
        accessToken: String,
        offset: Int?,
        limit: Int?,
        sortOrder: SortOrder?
    ): List<QuizResultResponse>

}

