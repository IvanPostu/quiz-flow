package com.iv127.quizflow.core.services.authentication

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authentication.Authentication

interface AuthenticationService {

    fun createAuthenticationRefreshToken(user: User): Authentication

    fun createAuthenticationAccessToken(refreshToken: String): Authentication

}
