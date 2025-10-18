package com.iv127.quizflow.core.rest.api.quiz

interface QuizzesRoutes {
    companion object {
        const val ROUTE_PATH: String = "/quizzes"
    }

    suspend fun get(accessToken: String, quizId: String): QuizResponse

    suspend fun create(accessToken: String, request: QuizCreateRequest): QuizResponse

    suspend fun update(accessToken: String, quizId: String, request: QuizUpdateRequest): QuizResponse

}
