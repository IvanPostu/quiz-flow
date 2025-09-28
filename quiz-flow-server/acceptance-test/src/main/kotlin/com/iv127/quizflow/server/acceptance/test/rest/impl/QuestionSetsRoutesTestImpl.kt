package com.iv127.quizflow.server.acceptance.test.rest.impl

import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import com.iv127.quizflow.core.rest.api.SortOrder
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetResponse
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetUpdateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class QuestionSetsRoutesTestImpl(
    private val client: HttpClient,
    private val config: GlobalConfig = GlobalConfig.INSTANCE
) : QuestionSetsRoutes {

    override suspend fun get(id: String): QuestionSetResponse {
        val response: HttpResponse = client.get("${config.baseUrl}/api/question-sets/$id") {
            contentType(ContentType.Application.Json)
        }
        return response.body<QuestionSetResponse>()
    }

    override suspend fun list(offset: Int, limit: Int, sortOrder: SortOrder): List<QuestionSetResponse> {
        val response: HttpResponse = client.get(
            """
            ${config.baseUrl}/api/question-sets?limit=$limit&offset=$offset&sortOrder=${sortOrder.name}
        """.trimIndent()
        ) {
            contentType(ContentType.Application.Json)
        }
        return response.body<List<QuestionSetResponse>>()
    }

    override suspend fun create(request: QuestionSetCreateRequest): QuestionSetResponse {
        val response: HttpResponse = client.post("${config.baseUrl}/api/question-sets") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.body<QuestionSetResponse>()
    }

    override suspend fun update(id: String, request: QuestionSetUpdateRequest): QuestionSetResponse {
        val response: HttpResponse = client.put("${config.baseUrl}/api/question-sets/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.body<QuestionSetResponse>()
    }

    override suspend fun archive(id: String): QuestionSetResponse {
        val response: HttpResponse = client.delete("${config.baseUrl}/api/question-sets/$id") {
            contentType(ContentType.Application.Json)
        }
        return response.body<QuestionSetResponse>()
    }

}
