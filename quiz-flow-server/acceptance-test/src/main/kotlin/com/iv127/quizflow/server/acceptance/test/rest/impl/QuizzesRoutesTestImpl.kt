package com.iv127.quizflow.server.acceptance.test.rest.impl

import com.iv127.quizflow.core.rest.api.authorization.ApiAuthorization
import com.iv127.quizflow.core.rest.api.quiz.QuizCreateRequest
import com.iv127.quizflow.core.rest.api.quiz.QuizResponse
import com.iv127.quizflow.core.rest.api.quiz.QuizUpdateRequest
import com.iv127.quizflow.core.rest.api.quiz.QuizzesRoutes
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class QuizzesRoutesTestImpl(
    private val config: GlobalConfig = GlobalConfig.INSTANCE
) : QuizzesRoutes {
    override suspend fun create(authorization: ApiAuthorization, request: QuizCreateRequest): QuizResponse {
        val response: HttpResponse = config.performRequest { client ->
            client.post("${config.baseUrl}/api${QuizzesRoutes.ROUTE_PATH}") {
                contentType(ContentType.Application.Json)
                bearerAuth(authorization.getToken())
                setBody(request)
            }
        }
        return response.body<QuizResponse>()
    }

    override suspend fun update(authorization: ApiAuthorization, request: QuizUpdateRequest): QuizResponse {
        val response: HttpResponse = config.performRequest { client ->
            client.put("${config.baseUrl}/api${QuizzesRoutes.ROUTE_PATH}") {
                contentType(ContentType.Application.Json)
                bearerAuth(authorization.getToken())
                setBody(request)
            }
        }
        return response.body<QuizResponse>()
    }
}
