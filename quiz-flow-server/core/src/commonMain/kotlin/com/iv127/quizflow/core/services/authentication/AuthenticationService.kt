package com.iv127.quizflow.core.services.authentication

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authentication.CreatedAuthenticationAccessToken
import com.iv127.quizflow.core.model.authentication.CreatedAuthenticationRefreshToken

interface AuthenticationService {

    fun createAuthenticationRefreshToken(user: User): CreatedAuthenticationRefreshToken

    fun createAuthenticationAccessToken(refreshToken: String): CreatedAuthenticationAccessToken

}
