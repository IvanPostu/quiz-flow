package com.iv127.quizflow.core.rest.api.quiz

import com.iv127.quizflow.core.rest.api.authorization.ApiAuthorization

interface QuizzesRoutes {
    companion object {
        const val ROUTE_PATH: String = "/quizzes"
    }

    suspend fun get(authorization: ApiAuthorization, quizId: String): QuizResponse

    suspend fun create(authorization: ApiAuthorization, request: QuizCreateRequest): QuizResponse

    suspend fun update(authorization: ApiAuthorization, quizId: String, request: QuizUpdateRequest): QuizResponse

}
