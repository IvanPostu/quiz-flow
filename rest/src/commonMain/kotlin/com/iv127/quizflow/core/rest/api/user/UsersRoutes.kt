package com.iv127.quizflow.core.rest.api.user

interface UsersRoutes {
    companion object {
        const val ROUTE_PATH: String = "/users"
    }

    fun list(): List<UserResponse>

    fun create(request: UserCreateRequest): UserResponse
}
