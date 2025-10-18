package com.iv127.quizflow.core.rest.impl.user

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authentication.AuthorizationScope
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.user.UserCreateRequest
import com.iv127.quizflow.core.rest.api.user.UserResponse
import com.iv127.quizflow.core.rest.api.user.UsersRoutes
import com.iv127.quizflow.core.rest.api.user.UsersRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.impl.exception.ApiClientErrorExceptionTranslator
import com.iv127.quizflow.core.rest.impl.exception.InvalidFieldValueException
import com.iv127.quizflow.core.security.AccessTokenProvider
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import com.iv127.quizflow.core.services.authentication.AuthenticationService
import com.iv127.quizflow.core.services.user.UserService
import com.iv127.quizflow.core.services.user.UsernameAlreadyTakenException
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.core.KoinApplication

class UsersRoutesImpl(koinApp: KoinApplication) : UsersRoutes, ApiRoute {

    private val userService: UserService by koinApp.koin.inject()
    private val authenticationService: AuthenticationService by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.get(ROUTE_PATH, routingContextWebResponse {
            val accessToken = AccessTokenProvider.provide(call)
            JsonWebResponse.create(list(accessToken))
        })
        parent.post(ROUTE_PATH, routingContextWebResponse {
            val request = call.receive<UserCreateRequest>()
            val accessToken = AccessTokenProvider.provide(call)
            JsonWebResponse.create(create(accessToken, request))
        })
    }

    override suspend fun list(accessToken: String): List<UserResponse> {
        authenticationService.checkAuthorizationScopes(accessToken, setOf(AuthorizationScope.SUPER_ADMIN))
        return userService.getAll()
            .map { mapToUserResponse(it) }
    }

    override suspend fun create(accessToken: String, request: UserCreateRequest): UserResponse {
        authenticationService.checkAuthorizationScopes(accessToken, setOf(AuthorizationScope.SUPER_ADMIN))
        try {
            if (request.username.isBlank()) {
                throw InvalidFieldValueException(
                    "username",
                    request.username,
                    "Empty value is not allowed"
                )
            }
            if (request.password.isBlank()) {
                throw InvalidFieldValueException(
                    "password",
                    "***",
                    "Empty value is not allowed"
                )
            }
            val user = userService.create(request.username, request.password)
            return mapToUserResponse(user)
        } catch (e: Exception) {
            throw ApiClientErrorExceptionTranslator
                .translateAndThrowOrElseFail(
                    e,
                    UsernameAlreadyTakenException::class,
                    InvalidFieldValueException::class
                )
        }
    }

    private fun mapToUserResponse(user: User) = UserResponse(user.id, user.username)
}
