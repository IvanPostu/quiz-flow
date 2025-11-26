package com.iv127.quizflow.core.rest.impl.authentication

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authentication.exceptions.AuthenticationInvalidCredentialsException
import com.iv127.quizflow.core.model.authentication.AuthorizationScope
import com.iv127.quizflow.core.model.authentication.exceptions.RefreshTokenIsInvalidException
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.authentication.AccessTokenResponse
import com.iv127.quizflow.core.rest.api.authentication.AccessTokenSummaryResponse
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes.Companion.REFRESHABLE_TOKEN_NAME
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.api.authentication.AuthorizationScopeResponse
import com.iv127.quizflow.core.rest.api.authentication.MarkAccessTokenAsExpiredRequest
import com.iv127.quizflow.core.rest.api.authentication.MarkRefreshTokenAsExpiredRequest
import com.iv127.quizflow.core.rest.api.authentication.RefreshTokenSummaryResponse
import com.iv127.quizflow.core.rest.api.authentication.TokenSummaryResponse
import com.iv127.quizflow.core.rest.api.authentication.UsernamePasswordAuthenticationRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieResponse
import com.iv127.quizflow.core.rest.cookie.CookieMapper
import com.iv127.quizflow.core.security.AccessTokenProvider
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import com.iv127.quizflow.core.services.authentication.AuthenticationService
import com.iv127.quizflow.core.services.user.UserInvalidPasswordException
import com.iv127.quizflow.core.services.user.UserNotFoundException
import com.iv127.quizflow.core.services.user.UserService
import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.util.date.GMTDate
import io.ktor.util.logging.KtorSimpleLogger
import kotlin.time.ExperimentalTime
import org.koin.core.KoinApplication

@OptIn(ExperimentalTime::class)
class AuthenticationsRoutesImpl(koinApp: KoinApplication) : AuthenticationsRoutes, ApiRoute {

    companion object {
        private val LOG = KtorSimpleLogger(getClassFullName(AuthenticationsRoutesImpl::class))
    }

    private val userService: UserService by koinApp.koin.inject()
    private val authenticationService: AuthenticationService by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.post("$ROUTE_PATH/sign-in", routingContextWebResponse {
            val request = call.receive<UsernamePasswordAuthenticationRequest>()
            val signInResult = signIn(request)
            signInResult.first.forEach {
                call.response.cookies
                    .append(CookieMapper.mapToCookie(it))
            }
            JsonWebResponse.create(signInResult.second)
        })
        parent.post("$ROUTE_PATH/sign-out", routingContextWebResponse {
            val cookies = call.request.cookies
            val cookieRequests = cookies.rawCookies.keys.mapNotNull { key ->
                val value = cookies[key]
                if (value == null) null else Pair(key, value)
            }.map { CookieMapper.mapToCookieRequest(it.first, it.second) }
            val signOutCookies = signOut(cookieRequests)
            signOutCookies.forEach {
                call.response.cookies
                    .append(CookieMapper.mapToCookie(it))
            }
            JsonWebResponse.empty()
        })
        parent.post("$ROUTE_PATH/access-token", routingContextWebResponse {
            val cookies = call.request.cookies
            val cookieRequests = cookies.rawCookies.keys.mapNotNull { key ->
                val value = cookies[key]
                if (value == null) null else Pair(key, value)
            }.map { CookieMapper.mapToCookieRequest(it.first, it.second) }
            JsonWebResponse.create(createAccessToken(cookieRequests))
        })
        parent.post("$ROUTE_PATH/access-token-lifetime", routingContextWebResponse {
            val accessToken = AccessTokenProvider.provide(call)
            JsonWebResponse.create(extendAccessTokenLifetime(accessToken))
        })
        parent.post("$ROUTE_PATH/refresh-token-expiration", routingContextWebResponse {
            val accessToken = AccessTokenProvider.provide(call)
            val request = call.receive<MarkRefreshTokenAsExpiredRequest>()
            JsonWebResponse.create(markRefreshTokenAsExpired(accessToken, request))
        })
        parent.post("$ROUTE_PATH/access-token-expiration", routingContextWebResponse {
            val accessToken = AccessTokenProvider.provide(call)
            val request = call.receive<MarkAccessTokenAsExpiredRequest>()
            JsonWebResponse.create(markAccessTokenAsExpired(accessToken, request))
        })
    }

    override suspend fun signIn(request: UsernamePasswordAuthenticationRequest):
        Pair<List<CookieResponse>, AccessTokenResponse> {

        val user: User = getUser(request)
        val authentication = authenticationService.createAuthenticationRefreshToken(user)
        val expiresGMTDate = GMTDate(
            authentication.authentication()
                .authenticationRefreshToken.expirationDate.toEpochMilliseconds()
        )
        val cookieResponseList = listOf(
            CookieResponse(
                name = REFRESHABLE_TOKEN_NAME,
                value = authentication.refreshToken(),
                expires = expiresGMTDate,
                path = "/",
                secure = false, // set true if using HTTPS
                httpOnly = true
            )
        )
        return Pair(
            cookieResponseList,
            AccessTokenResponse(
                authentication.authentication().authenticationAccessToken.id,
                authentication.authentication().authenticationRefreshToken.id,
                authentication.authentication().accessToken,
                authentication.authentication().authenticationRefreshToken.authorizationScopes.map { mapScope(it) }
                    .toSet(),
                authentication.authentication().authenticationAccessToken.expirationDate,
                authentication.authentication().authenticationRefreshToken.expirationDate,
            )
        )
    }

    override suspend fun signOut(cookies: List<CookieRequest>): List<CookieResponse> {
        val refreshToken = getRefreshTokenFromCookies(cookies)
        val refreshAuthentication =
            authenticationService.getAuthenticationRefreshTokenByRefreshTokenValue(refreshToken)
        authenticationService.markRefreshTokenAsExpired(refreshAuthentication.id)

        return listOf(
            CookieResponse(
                name = REFRESHABLE_TOKEN_NAME,
                value = "",
                maxAge = 0,
                expires = GMTDate.START,
            )
        )
    }

    override suspend fun createAccessToken(cookies: List<CookieRequest>): AccessTokenResponse =
        internalCreateAccessToken(cookies)


    override suspend fun extendAccessTokenLifetime(accessToken: String): AccessTokenResponse {
        val authentication = authenticationService.extendAccessTokenLifetime(accessToken)
        return AccessTokenResponse(
            authentication.authenticationAccessToken.id,
            authentication.authenticationRefreshToken.id,
            authentication.accessToken,
            authentication.authenticationRefreshToken.authorizationScopes.map { mapScope(it) }.toSet(),
            authentication.authenticationAccessToken.expirationDate,
            authentication.authenticationRefreshToken.expirationDate,
        )
    }

    override suspend fun markRefreshTokenAsExpired(
        accessToken: String,
        markRefreshTokenAsExpiredRequest: MarkRefreshTokenAsExpiredRequest
    ): RefreshTokenSummaryResponse {
        authenticationService.checkAuthorizationScopes(accessToken, setOf(AuthorizationScope.SUPER_ADMIN))
        val refreshTokenId = markRefreshTokenAsExpiredRequest.refreshTokenId
        val updatedRefreshToken = authenticationService.markRefreshTokenAsExpired(refreshTokenId)
        return RefreshTokenSummaryResponse(
            refreshTokenId = updatedRefreshToken.id,
            refreshTokenHash = updatedRefreshToken.refreshTokenHash,
            createdDate = updatedRefreshToken.createdDate,
            expirationDate = updatedRefreshToken.expirationDate,
            authorizationScopes = updatedRefreshToken.authorizationScopes.map { mapScope(it) }.toSet()
        )
    }

    override suspend fun markAccessTokenAsExpired(
        accessToken: String,
        markAccessTokenAsExpiredRequest: MarkAccessTokenAsExpiredRequest
    ): AccessTokenSummaryResponse {
        authenticationService.checkAuthorizationScopes(accessToken, setOf(AuthorizationScope.SUPER_ADMIN))
        val updatedAccessToken = authenticationService
            .markAccessTokenAsExpired(markAccessTokenAsExpiredRequest.accessTokenId)
        return AccessTokenSummaryResponse(
            accessTokenId = updatedAccessToken.id,
            accessTokenHash = updatedAccessToken.accessTokenHash,
            createdDate = updatedAccessToken.createdDate,
            expirationDate = updatedAccessToken.expirationDate,
        )
    }

    override suspend fun getUserTokens(
        accessToken: String,
        markAccessTokenAsExpiredRequest: MarkAccessTokenAsExpiredRequest
    ): TokenSummaryResponse {
        TODO("Not yet implemented")
    }

    private fun internalCreateAccessToken(cookies: List<CookieRequest>): AccessTokenResponse {
        val refreshToken = getRefreshTokenFromCookies(cookies)
        val authentication = authenticationService.createAuthenticationAccessToken(refreshToken)
            .authentication()
        return AccessTokenResponse(
            authentication.authenticationAccessToken.id,
            authentication.authenticationRefreshToken.id,
            authentication.accessToken,
            authentication.authenticationRefreshToken.authorizationScopes.map { mapScope(it) }.toSet(),
            authentication.authenticationAccessToken.expirationDate,
            authentication.authenticationRefreshToken.expirationDate,
        )
    }

    private fun getRefreshTokenFromCookies(cookies: List<CookieRequest>): String {
        val refreshTokenCookies = cookies
            .filter { it.name == REFRESHABLE_TOKEN_NAME }

        if (refreshTokenCookies.isEmpty()) {
            throw RefreshTokenIsInvalidException()
        } else if (refreshTokenCookies.size > 1) {
            throw IllegalStateException(
                "More than one refresh token with the name " +
                    "$REFRESHABLE_TOKEN_NAME was found in the cookie: $refreshTokenCookies"
            )
        }
        return refreshTokenCookies[0].value
    }

    private fun getUser(request: UsernamePasswordAuthenticationRequest): User {
        try {
            return userService.getByUsernameAndPassword(
                request.username,
                request.password
            )
        } catch (e: Exception) {
            if ((e is UserNotFoundException) || (e is UserInvalidPasswordException)) {
                throw AuthenticationInvalidCredentialsException()
            }
            throw IllegalStateException(e)
        }
    }


    private fun mapScope(scope: AuthorizationScope): AuthorizationScopeResponse = when (scope) {
        AuthorizationScope.SUPER_ADMIN -> AuthorizationScopeResponse.SUPER_ADMIN
        AuthorizationScope.ADMIN -> AuthorizationScopeResponse.ADMIN
        AuthorizationScope.REGULAR_USER -> AuthorizationScopeResponse.REGULAR_USER
    }
}
