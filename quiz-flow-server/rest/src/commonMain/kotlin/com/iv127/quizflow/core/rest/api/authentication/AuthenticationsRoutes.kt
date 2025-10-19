package com.iv127.quizflow.core.rest.api.authentication

import com.iv127.quizflow.core.rest.api.cookie.CookieRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieResponse

interface AuthenticationsRoutes {

    companion object {
        const val ROUTE_PATH: String = "/authentications"

        const val REFRESHABLE_TOKEN_NAME = "refreshable_token"
    }

    suspend fun signIn(request: UsernamePasswordAuthenticationRequest): Pair<List<CookieResponse>, AccessTokenResponse>

    suspend fun createAccessToken(cookies: List<CookieRequest>): AccessTokenResponse

    suspend fun extendAccessTokenLifetime(accessToken: String): AccessTokenResponse

    /**
     * Super admin only
     */
    suspend fun markRefreshTokenAsExpired(
        accessToken: String,
        markRefreshTokenAsExpiredRequest: MarkRefreshTokenAsExpiredRequest
    ): RefreshTokenSummaryResponse

    /**
     * Super admin only
     */
    suspend fun markAccessTokenAsExpired(
        accessToken: String,
        markAccessTokenAsExpiredRequest: MarkAccessTokenAsExpiredRequest
    ): RefreshTokenSummaryResponse

    /**
     * Super admin only
     */
    suspend fun getUserTokens(
        accessToken: String,
        markAccessTokenAsExpiredRequest: MarkAccessTokenAsExpiredRequest
    ): List<RefreshTokenSummaryResponse>

}
