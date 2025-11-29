package com.iv127.quizflow.server.acceptance.test.rest.impl

import com.iv127.quizflow.core.rest.api.SortOrder
import com.iv127.quizflow.core.rest.api.quizresult.QuizFinalizedStateType
import com.iv127.quizflow.core.rest.api.quizresult.QuizResultResponse
import com.iv127.quizflow.core.rest.api.quizresult.QuizResultsRoutes
import com.iv127.quizflow.core.rest.api.quizresult.QuizResultsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType

class QuizResultsRoutesTestImpl(
    private val config: GlobalConfig = GlobalConfig.INSTANCE
) : QuizResultsRoutes {

    override suspend fun get(accessToken: String, quizId: String): QuizResultResponse {
        val response: HttpResponse = config.performRequest { client ->
            val url: Url = URLBuilder("${config.baseUrl}/api${ROUTE_PATH}/${quizId}").build()
            client.get(url) {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
            }
        }
        return response.body<QuizResultResponse>()
    }

    override suspend fun list(
        accessToken: String,
        offset: Int?,
        limit: Int?,
        sortOrder: SortOrder?,
        quizFinalizedStateType: QuizFinalizedStateType?,
    ): List<QuizResultResponse> {
        val response: HttpResponse = config.performRequest { client ->
            val url: Url = URLBuilder("${config.baseUrl}/api${ROUTE_PATH}")
                .apply {
                    if (offset != null) {
                        parameters.append("offset", offset.toString())
                    }
                    if (limit != null) {
                        parameters.append("limit", limit.toString())
                    }
                    if (sortOrder != null) {
                        parameters.append("sortOrder", sortOrder.toString())
                    }
                    if (quizFinalizedStateType != null) {
                        parameters.append("quizFinalizedStateType", quizFinalizedStateType.toString())
                    }
                }
                .build()
            client.get(url) {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
            }
        }
        return response.body<List<QuizResultResponse>>()
    }
}
