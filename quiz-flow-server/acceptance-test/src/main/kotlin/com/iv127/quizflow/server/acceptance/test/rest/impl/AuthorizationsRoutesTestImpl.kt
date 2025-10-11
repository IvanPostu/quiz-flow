package com.iv127.quizflow.server.acceptance.test.rest.impl

import com.iv127.quizflow.core.rest.api.authorization.AuthorizationResponse
import com.iv127.quizflow.core.rest.api.authorization.AuthorizationsRoutes
import com.iv127.quizflow.core.rest.api.authorization.UsernamePasswordAuthorizationRequest
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthorizationsRoutesTestImpl(
    private val config: GlobalConfig = GlobalConfig.INSTANCE
) : AuthorizationsRoutes {
    override suspend fun authorize(usernamePasswordAuthorizationRequest: UsernamePasswordAuthorizationRequest): AuthorizationResponse {
        val response: HttpResponse = config.performRequest { client ->
            client.post("${config.baseUrl}/api${AuthorizationsRoutes.ROUTE_PATH}") {
                contentType(ContentType.Application.Json)
                setBody(usernamePasswordAuthorizationRequest)
            }
        }
        return response.body<AuthorizationResponse>()
    }
}
