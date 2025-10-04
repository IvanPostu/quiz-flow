package com.iv127.quizflow.core.rest.impl.user

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.authorization.ApiAuthorization
import com.iv127.quizflow.core.rest.api.user.UserCreateRequest
import com.iv127.quizflow.core.rest.api.user.UserResponse
import com.iv127.quizflow.core.rest.api.user.UsersRoutes
import com.iv127.quizflow.core.rest.api.user.UsersRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.impl.ApiClientErrorException
import com.iv127.quizflow.core.security.AuthenticationProvider
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import com.iv127.quizflow.core.services.user.UserService
import com.iv127.quizflow.core.services.user.UsernameAlreadyTakenException
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.core.KoinApplication

class UsersRoutesImpl(koinApp: KoinApplication) : UsersRoutes, ApiRoute {

    private val userService: UserService by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.get(ROUTE_PATH, routingContextWebResponse {
            val authorization = AuthenticationProvider.provide(call)
            JsonWebResponse.create(list(authorization))
        })
        parent.post(ROUTE_PATH, routingContextWebResponse {
            val request = call.receive<UserCreateRequest>()
            val authorization = AuthenticationProvider.provide(call)
            JsonWebResponse.create(create(authorization, request))
        })
    }

    override suspend fun list(authorization: ApiAuthorization): List<UserResponse> {
        return userService.getAll()
            .map { mapToUserResponse(it) }
    }

    override suspend fun create(authorization: ApiAuthorization, request: UserCreateRequest): UserResponse {
        if (request.username.isBlank()) {
            throw ApiClientErrorException(
                "invalid_username",
                "Username is invalid"
            )
        }
        if (request.password.isBlank()) {
            throw ApiClientErrorException(
                "invalid_password",
                "Password is invalid"
            )
        }
        val user: User

        try {
            user = userService.create(request.username, request.password)
        } catch (e: UsernameAlreadyTakenException) {
            throw ApiClientErrorException(
                "username_was_already_taken",
                "An user with such username already exists"
            )
        }
        return mapToUserResponse(user)
    }

    private fun mapToUserResponse(user: User) = UserResponse(user.id, user.username)
}
