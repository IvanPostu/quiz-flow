package com.iv127.quizflow.core.rest.api.questionset

interface QuestionSetsRoutes {

    companion object {
        const val ROUTE_PATH: String = "/question-sets"
    }

    suspend fun get(id: String): QuestionSetResponse

    suspend fun list(): List<QuestionSetResponse>

    suspend fun create(request: QuestionSetCreateRequest): QuestionSetResponse

    suspend fun update(id: String, request: QuestionSetUpdateRequest): QuestionSetResponse

    suspend fun archive(id: String): QuestionSetResponse

}
