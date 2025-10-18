package com.iv127.quizflow.server.acceptance.test.rest.impl

import com.iv127.quizflow.core.rest.api.user.UserCreateRequest
import com.iv127.quizflow.core.rest.api.user.UserResponse
import com.iv127.quizflow.core.rest.api.user.UsersRoutes
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class UsersRoutesTestImpl(
    private val config: GlobalConfig = GlobalConfig.INSTANCE
) : UsersRoutes {
    override suspend fun list(accessToken: String): List<UserResponse> {
        val response: HttpResponse = config.performRequest { client ->
            client.get("${config.baseUrl}/api${UsersRoutes.ROUTE_PATH}") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
            }
        }
        return response.body<List<UserResponse>>()
    }

    override suspend fun create(accessToken: String, request: UserCreateRequest): UserResponse {
        val response: HttpResponse = config.performRequest { client ->
            client.post("${config.baseUrl}/api${UsersRoutes.ROUTE_PATH}") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
                setBody(request)
            }
        }
        return response.body<UserResponse>()
    }
}
