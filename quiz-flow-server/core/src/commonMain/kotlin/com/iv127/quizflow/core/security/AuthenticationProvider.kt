package com.iv127.quizflow.core.security

import com.iv127.quizflow.core.ktor.AuthorizationPlugin
import com.iv127.quizflow.core.model.authorization.Authorization
import io.ktor.server.application.ApplicationCall

object AuthenticationProvider {
    fun provide(call: ApplicationCall): Authorization {
        val authorization = call.attributes[AuthorizationPlugin.AUTHORIZATION_KEY]
        if(authorization is Authorization) {
            return authorization
        }
        throw AuthenticationException()
    }
}
