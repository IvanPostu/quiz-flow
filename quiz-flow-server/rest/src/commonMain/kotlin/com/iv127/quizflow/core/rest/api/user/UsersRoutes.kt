package com.iv127.quizflow.core.rest.api.user

interface UsersRoutes {
    companion object {
        const val ROUTE_PATH: String = "/users"
    }

    suspend fun list(accessToken: String): List<UserResponse>

    suspend fun create(accessToken: String, request: UserCreateRequest): UserResponse
}

