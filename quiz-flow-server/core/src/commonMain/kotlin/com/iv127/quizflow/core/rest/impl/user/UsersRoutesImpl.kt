package com.iv127.quizflow.core.rest.impl.user

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.user.UserCreateRequest
import com.iv127.quizflow.core.rest.api.user.UserResponse
import com.iv127.quizflow.core.rest.api.user.UsersRoutes
import com.iv127.quizflow.core.rest.api.user.UsersRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import com.iv127.quizflow.core.services.user.UserService
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.core.KoinApplication

class UsersRoutesImpl(val koinApp: KoinApplication) : UsersRoutes, ApiRoute {

    private val userService: UserService by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.get(ROUTE_PATH, webResponse {
            JsonWebResponse.create(list())
        })
        parent.post(ROUTE_PATH, webResponse {
            val request = call.receive<UserCreateRequest>()
            JsonWebResponse.create(create(request))
        })
    }

    override fun list(): List<UserResponse> {
        return userService.getAll()
            .map { mapToUserResponse(it) }
    }

    override fun create(request: UserCreateRequest): UserResponse {
        if (request.username.isBlank()) {
            throw IllegalArgumentException("username field shouldn't be blank")
        }
        if (request.password.isBlank()) {
            throw IllegalArgumentException("password field shouldn't be blank")
        }
        val user = userService.create(request.username, request.password)
        return mapToUserResponse(user)
    }

    private fun mapToUserResponse(user: User) = UserResponse(user.id, user.username)
}
