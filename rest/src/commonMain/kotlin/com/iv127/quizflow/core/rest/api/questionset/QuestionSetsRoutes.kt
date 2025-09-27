package com.iv127.quizflow.core.rest.api.questionset

import com.iv127.quizflow.core.rest.api.ApiRoute

interface QuestionSetsRoutes : ApiRoute {

    companion object {
        const val ROUTE_PATH: String = "/question-sets"
    }

    fun get(id: String): QuestionSetResponse

    fun list(): List<QuestionSetResponse>

    fun create(request: QuestionSetCreateRequest): QuestionSetResponse

    fun update(id: String, request: QuestionSetUpdateRequest): QuestionSetResponse

    fun archive(id: String): QuestionSetResponse

}
