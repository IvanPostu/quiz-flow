package com.iv127.quizflow.core.rest.api.user

import com.iv127.quizflow.core.rest.api.authorization.ApiAuthorization

interface UsersRoutes {
    companion object {
        const val ROUTE_PATH: String = "/users"
    }

    suspend fun list(authorization: ApiAuthorization): List<UserResponse>

    suspend fun create(authorization: ApiAuthorization, request: UserCreateRequest): UserResponse
}

