package com.iv127.quizflow.core.rest.impl.authentication

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authentication.AuthorizationScope
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.authentication.AccessTokenResponse
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes.Companion.REFRESHABLE_TOKEN_NAME
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.api.authentication.AuthorizationScopeResponse
import com.iv127.quizflow.core.rest.api.authentication.UsernamePasswordAuthenticationRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieResponse
import com.iv127.quizflow.core.rest.cookie.CookieMapper
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import com.iv127.quizflow.core.services.authentication.AuthenticationService
import com.iv127.quizflow.core.services.user.UserService
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.util.date.GMTDate
import kotlin.time.ExperimentalTime
import org.koin.core.KoinApplication

@OptIn(ExperimentalTime::class)
class AuthenticationsRoutesImpl(koinApp: KoinApplication) : AuthenticationsRoutes, ApiRoute {

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
    }

    override suspend fun signIn(request: UsernamePasswordAuthenticationRequest): Pair<List<CookieResponse>, AccessTokenResponse> {
        val user: User = userService.getByUsernameAndPassword(
            request.username,
            request.password
        )
        val created = authenticationService.createAuthenticationRefreshToken(user)
        val authenticationRefreshToken = created.authenticationRefreshToken
        val cookieResponseList = listOf(
            CookieResponse(
                name = REFRESHABLE_TOKEN_NAME,
                value = created.plainRefreshToken,
                expires = GMTDate(authenticationRefreshToken.expirationDate.toEpochMilliseconds()),
                path = "$ROUTE_PATH/access-token",
                secure = false, // set true if using HTTPS
                httpOnly = true
            )
        )
        val authenticationAccessToken = authenticationService.createAuthenticationAccessToken(created.plainRefreshToken)
        return Pair(
            cookieResponseList,
            AccessTokenResponse(
                authenticationAccessToken.accessToken,
                authenticationAccessToken.authorizationScopes.map { mapScope(it) }.toSet()
            )
        )
    }

    override suspend fun createAccessToken(cookies: List<CookieRequest>): AccessTokenResponse {
        val refreshTokenCookies = cookies
            .filter { it.name == REFRESHABLE_TOKEN_NAME }

        if (refreshTokenCookies.isEmpty()) {
            throw IllegalStateException("Access denied, refresh token wasn't found in cookie")
        } else if (refreshTokenCookies.size > 1) {
            throw IllegalStateException("Access denied, more than one refresh token was found in cookie")
        }
        val refreshToken = refreshTokenCookies[0].value

        val authenticationAccessToken = authenticationService.createAuthenticationAccessToken(refreshToken)
        return AccessTokenResponse(
            authenticationAccessToken.accessToken,
            authenticationAccessToken.authorizationScopes.map { mapScope(it) }.toSet()
        )
    }

    private fun mapScope(scope: AuthorizationScope): AuthorizationScopeResponse = when (scope) {
        AuthorizationScope.SUPER_ADMIN -> AuthorizationScopeResponse.SUPER_ADMIN
        AuthorizationScope.ADMIN -> AuthorizationScopeResponse.ADMIN
        AuthorizationScope.REGULAR_USER -> AuthorizationScopeResponse.REGULAR_USER
    }
}
