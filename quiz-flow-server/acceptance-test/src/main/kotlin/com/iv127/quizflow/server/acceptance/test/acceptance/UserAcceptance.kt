package com.iv127.quizflow.server.acceptance.test.acceptance

import com.iv127.quizflow.core.rest.api.user.UserCreateRequest
import com.iv127.quizflow.core.rest.api.user.UserResponse
import com.iv127.quizflow.core.rest.api.user.UsersRoutes
import com.iv127.quizflow.server.acceptance.test.rest.impl.UsersRoutesTestImpl

object UserAcceptance {

    private val userRoutes: UsersRoutes = UsersRoutesTestImpl()

    suspend fun createUser(): UserResponse {
        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        return createUser(username, password)
    }

    suspend fun createUser(username: String, password: String): UserResponse {
        val auth = AuthenticationAcceptance.authenticateSuperUser()
        val createdUser = userRoutes.create(auth.accessToken, UserCreateRequest(username, password))
        return createdUser
    }

}
