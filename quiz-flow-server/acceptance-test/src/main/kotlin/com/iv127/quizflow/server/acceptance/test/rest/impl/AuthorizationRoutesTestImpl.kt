package com.iv127.quizflow.server.acceptance.test.rest.impl

import com.iv127.quizflow.core.rest.api.authorization.AuthorizationResponse
import com.iv127.quizflow.core.rest.api.authorization.AuthorizationRoutes
import com.iv127.quizflow.core.rest.api.authorization.UsernamePasswordAuthorizationRequest
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthorizationRoutesTestImpl(
    private val client: HttpClient,
    private val config: GlobalConfig = GlobalConfig.INSTANCE
) : AuthorizationRoutes {
    override suspend fun authorize(usernamePasswordAuthorizationRequest: UsernamePasswordAuthorizationRequest): AuthorizationResponse {
        val response: HttpResponse = client.post("${config.baseUrl}/api/${AuthorizationRoutes.ROUTE_PATH}") {
            contentType(ContentType.Application.Json)
            setBody(usernamePasswordAuthorizationRequest)
        }
        return response.body<AuthorizationResponse>()
    }
}
