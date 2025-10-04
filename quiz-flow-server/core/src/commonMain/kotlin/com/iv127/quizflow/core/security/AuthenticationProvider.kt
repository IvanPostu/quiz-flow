package com.iv127.quizflow.core.security

import com.iv127.quizflow.core.ktor.AuthorizationPlugin
import com.iv127.quizflow.core.model.authorization.Authorization
import com.iv127.quizflow.core.rest.api.authorization.AnonymousAuthorization
import io.ktor.server.application.ApplicationCall

object AuthenticationProvider {
    fun provide(call: ApplicationCall): Authorization {
        val authorization = call.attributes[AuthorizationPlugin.AUTHORIZATION_KEY]
        if (authorization is Authorization) {
            return authorization
        }
        if (authorization is AnonymousAuthorization) {
            throw AuthenticationException(authorization.message)
        }
        throw AuthenticationException()
    }
}
