package com.iv127.quizflow.core.rest.api.questionset

import com.iv127.quizflow.core.rest.api.SortOrder

interface QuestionSetsRoutes {

    companion object {
        const val ROUTE_PATH: String = "/question-sets"
    }

    suspend fun get(accessToken: String, id: String): QuestionSetResponse

    suspend fun list(accessToken: String, offset: Int, limit: Int, sortOrder: SortOrder): List<QuestionSetResponse>

    suspend fun listGlobal(
        accessToken: String,
        offset: Int,
        limit: Int,
        sortOrder: SortOrder
    ): List<QuestionSetResponse>

    suspend fun create(accessToken: String, request: QuestionSetCreateRequest): QuestionSetResponse

    suspend fun update(accessToken: String, id: String, request: QuestionSetUpdateRequest): QuestionSetResponse

    suspend fun archive(accessToken: String, id: String): QuestionSetResponse

}
