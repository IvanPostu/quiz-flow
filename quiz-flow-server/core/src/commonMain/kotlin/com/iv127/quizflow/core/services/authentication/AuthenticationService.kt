package com.iv127.quizflow.core.services.authentication

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authentication.Authentication
import com.iv127.quizflow.core.model.authentication.AuthorizationScope

interface AuthenticationService {

    fun createAuthenticationRefreshToken(user: User): AuthenticationWithRefreshToken

    fun createAuthenticationAccessToken(refreshToken: String): AuthenticationWithRefreshToken

    fun checkAuthorizationScopes(accessToken: String, requiredScopes: Set<AuthorizationScope>)

    fun checkAuthorizationScopes(authentication: Authentication, requiredScopes: Set<AuthorizationScope>)

    fun getAuthenticationByAccessToken(accessToken: String): Authentication

    interface AuthenticationWithRefreshToken {
        fun authentication(): Authentication
        fun refreshToken(): String
    }

}
