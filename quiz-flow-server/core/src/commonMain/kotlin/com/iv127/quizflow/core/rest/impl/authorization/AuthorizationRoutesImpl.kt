package com.iv127.quizflow.core.rest.impl.authorization

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authorization.Authorization
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.authorization.AuthorizationResponse
import com.iv127.quizflow.core.rest.api.authorization.AuthorizationRoutes
import com.iv127.quizflow.core.rest.api.authorization.AuthorizationRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.api.authorization.AuthorizationScopeResponse
import com.iv127.quizflow.core.rest.api.authorization.UsernamePasswordAuthorizationRequest
import com.iv127.quizflow.core.security.AuthenticationException
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import com.iv127.quizflow.core.services.authorization.AuthorizationService
import com.iv127.quizflow.core.services.user.UserInvalidPasswordException
import com.iv127.quizflow.core.services.user.UserNotFoundException
import com.iv127.quizflow.core.services.user.UserService
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlin.time.ExperimentalTime
import org.koin.core.KoinApplication

@OptIn(ExperimentalTime::class)
class AuthorizationRoutesImpl(koinApp: KoinApplication) : AuthorizationRoutes, ApiRoute {

    private val authorizationService: AuthorizationService by koinApp.koin.inject()
    private val userService: UserService by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.post(ROUTE_PATH, routingContextWebResponse {
            val request = call.receive<UsernamePasswordAuthorizationRequest>()
            JsonWebResponse.create(authorize(request))
        })
    }

    override suspend fun authorize(
        usernamePasswordAuthorizationRequest: UsernamePasswordAuthorizationRequest
    ): AuthorizationResponse {
        try {
            return internalAuthorize(usernamePasswordAuthorizationRequest)
        } catch (e: Exception) {
            if ((e is UserNotFoundException) || (e is UserInvalidPasswordException)) {
                throw AuthenticationException("Username of password is invalid")
            }
            throw IllegalStateException(e)
        }
    }

    private fun internalAuthorize(
        usernamePasswordAuthorizationRequest: UsernamePasswordAuthorizationRequest
    ): AuthorizationResponse {
        val user: User = userService.getByUsernameAndPassword(
            usernamePasswordAuthorizationRequest.username,
            usernamePasswordAuthorizationRequest.password
        )
        val authorization = authorizationService.create(user, null)
        return mapToResponse(authorization)
    }

    private fun mapToResponse(authorization: Authorization): AuthorizationResponse {
        return AuthorizationResponse(
            authorization.id,
            authorization.accessToken,
            authorization.createdDate,
            authorization.expirationDate,
            authorization.userId,
            authorization.authorizationScopes.map { AuthorizationScopeResponse.valueOf(it.name) }
        )
    }


}
