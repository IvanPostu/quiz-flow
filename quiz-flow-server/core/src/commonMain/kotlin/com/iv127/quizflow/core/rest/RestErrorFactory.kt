package com.iv127.quizflow.core.rest

import com.iv127.quizflow.core.lang.IdGenerator
import com.iv127.quizflow.core.rest.api.error.RestErrorResponse

class RestErrorFactory {
    companion object {
        private val generateId: () -> String = { IdGenerator.getValue() }

        fun createAuthenticationClientError(reason: String?): RestErrorResponse = RestErrorResponse(
            generateId(),
            "authentication_error",
            "Authentication failed",
            mapOf("reason" to (reason ?: ""))
        )

        fun createServerError(): RestErrorResponse = RestErrorResponse(
            generateId(),
            "server_error",
            "An unexpected error occurred",
            mapOf()
        )
    }
}
