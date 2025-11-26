package com.iv127.quizflow.core.security

import com.iv127.quizflow.core.ktor.AuthenticationPlugin
import com.iv127.quizflow.core.model.authentication.exceptions.AccessTokenMissingException
import io.ktor.server.application.ApplicationCall

object AccessTokenProvider {
    fun provide(call: ApplicationCall): String {
        val accessToken = call.attributes[AuthenticationPlugin.AUTHENTICATION_KEY]
        if (accessToken.isBlank()) {
            throw AccessTokenMissingException()
        }
        return accessToken
    }
}
