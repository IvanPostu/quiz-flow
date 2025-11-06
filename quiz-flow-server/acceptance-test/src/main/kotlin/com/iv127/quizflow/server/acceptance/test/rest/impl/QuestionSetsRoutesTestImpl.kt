package com.iv127.quizflow.server.acceptance.test.rest.impl

import com.iv127.quizflow.core.rest.api.SortOrder
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetResponse
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetUpdateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class QuestionSetsRoutesTestImpl(
    private val config: GlobalConfig = GlobalConfig.INSTANCE
) : QuestionSetsRoutes {

    override suspend fun get(accessToken: String, id: String): QuestionSetResponse {
        val response: HttpResponse = config.performRequest { client ->
            client.get("${config.baseUrl}/api${QuestionSetsRoutes.ROUTE_PATH}/$id") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
            }
        }
        return response.body<QuestionSetResponse>()
    }

    override suspend fun list(
        accessToken: String,
        offset: Int,
        limit: Int,
        sortOrder: SortOrder
    ): List<QuestionSetResponse> {
        val response: HttpResponse = config.performRequest { client ->
            client.get(
                """
                    ${config.baseUrl}/api${QuestionSetsRoutes.ROUTE_PATH}?limit=$limit&offset=$offset&sortOrder=${sortOrder.name}
                """.trimIndent()
            ) {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
            }

        }
        return response.body<List<QuestionSetResponse>>()
    }

    override suspend fun create(accessToken: String, request: QuestionSetCreateRequest): QuestionSetResponse {
        val response: HttpResponse = config.performRequest { client ->
            client.post("${config.baseUrl}/api${QuestionSetsRoutes.ROUTE_PATH}") {
                contentType(ContentType.Application.Json)
                setBody(request)
                bearerAuth(accessToken)
            }
        }
        return response.body<QuestionSetResponse>()
    }

    override suspend fun update(
        accessToken: String,
        id: String,
        request: QuestionSetUpdateRequest
    ): QuestionSetResponse {
        val response: HttpResponse = config.performRequest { client ->
            client.put("${config.baseUrl}/api${QuestionSetsRoutes.ROUTE_PATH}/$id") {
                contentType(ContentType.Application.Json)
                setBody(request)
                bearerAuth(accessToken)
            }
        }
        return response.body<QuestionSetResponse>()
    }

    override suspend fun archive(accessToken: String, id: String): QuestionSetResponse {
        val response: HttpResponse = config.performRequest { client ->
            client.delete("${config.baseUrl}/api${QuestionSetsRoutes.ROUTE_PATH}/$id") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
            }
        }
        return response.body<QuestionSetResponse>()
    }

}
