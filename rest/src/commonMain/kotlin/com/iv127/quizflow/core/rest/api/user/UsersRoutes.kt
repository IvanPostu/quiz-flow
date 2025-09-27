package com.iv127.quizflow.core.rest.api.user

import com.iv127.quizflow.core.rest.api.ApiRoute

interface UsersRoutes : ApiRoute {
    companion object {
        val ROUTE_PATH: String = "/users"
    }

    fun list(): List<UserResponse>

    fun create(request: UserCreateRequest): UserResponse
}
