package com.iv127.quizflow.core.rest.impl.authentication

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authentication.AuthenticationAccessTokenNotFoundException
import com.iv127.quizflow.core.model.authentication.AuthenticationRefreshTokenNotFoundException
import com.iv127.quizflow.core.model.authentication.AuthorizationScope
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.authentication.AccessTokenResponse
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes.Companion.REFRESHABLE_TOKEN_NAME
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.api.authentication.AuthorizationScopeResponse
import com.iv127.quizflow.core.rest.api.authentication.MarkAccessTokenAsExpiredRequest
import com.iv127.quizflow.core.rest.api.authentication.MarkRefreshTokenAsExpiredRequest
import com.iv127.quizflow.core.rest.api.authentication.RefreshTokenSummaryResponse
import com.iv127.quizflow.core.rest.api.authentication.UsernamePasswordAuthenticationRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieResponse
import com.iv127.quizflow.core.rest.cookie.CookieMapper
import com.iv127.quizflow.core.security.AccessTokenProvider
import com.iv127.quizflow.core.security.AuthenticationException
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import com.iv127.quizflow.core.services.authentication.AuthenticationService
import com.iv127.quizflow.core.services.user.UserInvalidPasswordException
import com.iv127.quizflow.core.services.user.UserNotFoundException
import com.iv127.quizflow.core.services.user.UserService
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.util.date.GMTDate
import kotlin.time.ExperimentalTime
import org.koin.core.KoinApplication

@OptIn(ExperimentalTime::class)
class AuthenticationsRoutesImpl(koinApp: KoinApplication) : AuthenticationsRoutes, ApiRoute {

    companion object {
        private const val REFRESHABLE_TOKEN_COOKIE_PATH = "/api$ROUTE_PATH/access-token"
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
    }

    override suspend fun signIn(request: UsernamePasswordAuthenticationRequest):
        Pair<List<CookieResponse>, AccessTokenResponse> = internalSignIn(request)

    override suspend fun createAccessToken(cookies: List<CookieRequest>): AccessTokenResponse {
        try {
            return internalCreateAccessToken(cookies)
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: Exception) {
            if (e is AuthenticationRefreshTokenNotFoundException) {
                throw AuthenticationException("Refreshable token is invalid")
            }
            throw IllegalStateException(e)
        }
    }

    override suspend fun extendAccessTokenLifetime(accessToken: String): AccessTokenResponse {
        val authentication = try {
            authenticationService.extendAccessTokenLifetime(accessToken)
        } catch (e: AuthenticationAccessTokenNotFoundException) {
            throw AuthenticationException("Access token is invalid")
        }
        return AccessTokenResponse(
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
        TODO("Not yet implemented")
    }

    override suspend fun markAccessTokenAsExpired(
        accessToken: String,
        markAccessTokenAsExpiredRequest: MarkAccessTokenAsExpiredRequest
    ): RefreshTokenSummaryResponse {
        TODO("Not yet implemented")
    }

    override suspend fun getUserTokens(
        accessToken: String,
        markAccessTokenAsExpiredRequest: MarkAccessTokenAsExpiredRequest
    ): List<RefreshTokenSummaryResponse> {
        TODO("Not yet implemented")
    }

    private fun internalCreateAccessToken(cookies: List<CookieRequest>): AccessTokenResponse {
        val refreshTokenCookies = cookies
            .filter { it.name == REFRESHABLE_TOKEN_NAME }

        if (refreshTokenCookies.isEmpty()) {
            throw AuthenticationException("Refresh token is not found in the cookie")
        } else if (refreshTokenCookies.size > 1) {
            throw IllegalStateException(
                "More than one refresh token with the name " +
                    "$REFRESHABLE_TOKEN_NAME was found in cookie: $refreshTokenCookies"
            )
        }
        val refreshToken = refreshTokenCookies[0].value

        val authentication = authenticationService.createAuthenticationAccessToken(refreshToken)
            .authentication()
        return AccessTokenResponse(
            authentication.accessToken,
            authentication.authenticationRefreshToken.authorizationScopes.map { mapScope(it) }.toSet(),
            authentication.authenticationAccessToken.expirationDate,
            authentication.authenticationRefreshToken.expirationDate,
        )
    }


    private fun internalSignIn(request: UsernamePasswordAuthenticationRequest): Pair<List<CookieResponse>, AccessTokenResponse> {
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
                path = REFRESHABLE_TOKEN_COOKIE_PATH,
                secure = false, // set true if using HTTPS
                httpOnly = true
            )
        )
        return Pair(
            cookieResponseList,
            AccessTokenResponse(
                authentication.authentication().accessToken,
                authentication.authentication().authenticationRefreshToken.authorizationScopes.map { mapScope(it) }
                    .toSet(),
                authentication.authentication().authenticationAccessToken.expirationDate,
                authentication.authentication().authenticationRefreshToken.expirationDate,
            )
        )
    }

    private fun getUser(request: UsernamePasswordAuthenticationRequest): User {
        try {
            return userService.getByUsernameAndPassword(
                request.username,
                request.password
            )
        } catch (e: Exception) {
            if ((e is UserNotFoundException) || (e is UserInvalidPasswordException)) {
                throw AuthenticationException("Username of password is invalid")
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
