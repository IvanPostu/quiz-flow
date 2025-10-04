package com.iv127.quizflow.core.rest.api.user

import com.iv127.quizflow.core.rest.api.authorization.ApiAuthorization

interface UsersRoutes {
    companion object {
        const val ROUTE_PATH: String = "/users"
    }

    fun list(): List<UserResponse>

    fun create(authorization: ApiAuthorization, request: UserCreateRequest): UserResponse
}

