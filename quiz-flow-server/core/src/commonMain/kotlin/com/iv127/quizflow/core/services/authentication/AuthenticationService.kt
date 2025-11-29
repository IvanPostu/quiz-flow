package com.iv127.quizflow.core.services.authentication

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authentication.Authentication
import com.iv127.quizflow.core.model.authentication.AuthenticationAccessToken
import com.iv127.quizflow.core.model.authentication.AuthenticationRefreshToken
import com.iv127.quizflow.core.model.authentication.AuthorizationScope

interface AuthenticationService {

    companion object {
        const val SUPER_USER_ID = "902fb9f1-0199-1000-edfb-0000ad471c87"
    }

    fun createAuthenticationRefreshToken(user: User): AuthenticationWithRefreshToken

    fun createAuthenticationAccessToken(refreshToken: String): AuthenticationWithRefreshToken

    fun checkAuthorizationScopes(accessToken: String, requiredScopes: Set<AuthorizationScope>)

    fun checkAuthorizationScopes(authentication: Authentication, requiredScopes: Set<AuthorizationScope>)

    fun getAuthenticationRefreshTokenByRefreshTokenValue(refreshToken: String): AuthenticationRefreshToken

    fun getAuthenticationByAccessToken(accessToken: String): Authentication

    fun getAuthenticationByRefreshTokenId(refreshTokenId: String): AuthenticationRefreshToken

    fun getAuthenticationByAccessTokenId(accessTokenId: String): AuthenticationAccessToken

    fun markRefreshTokenAsExpired(refreshTokenId: String): AuthenticationRefreshToken

    fun markAccessTokenAsExpired(accessTokenId: String): AuthenticationAccessToken

    fun extendAccessTokenLifetime(accessToken: String): Authentication

    fun getRefreshTokenSummary(refreshTokenId: String): Pair<AuthenticationRefreshToken, List<AuthenticationAccessToken>>

    interface AuthenticationWithRefreshToken {
        fun authentication(): Authentication
        fun refreshToken(): String
    }

}
