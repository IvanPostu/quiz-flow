package com.iv127.quizflow.core.ktor

import com.iv127.quizflow.core.rest.RestErrorFactory
import com.iv127.quizflow.core.rest.impl.ApiClientErrorException
import com.iv127.quizflow.core.security.AuthenticationException
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.util.logging.KtorSimpleLogger

object CustomStatusPagesConfig {
    private val LOG = KtorSimpleLogger(getClassFullName(CustomStatusPagesConfig::class))

    fun configure(): StatusPagesConfig.() -> Unit {
        return {
            exception<Throwable> { call, exception ->
                if (exception is AuthenticationException) {
                    val clientError = RestErrorFactory.createAuthenticationClientError(exception.reason)
                    LOG.warn("Client error was caught, uniqueId:${clientError.uniqueId}", exception)
                    webResponse(call) {
                        JsonWebResponse.create(
                            body = clientError,
                            status = HttpStatusCode.Unauthorized
                        )
                    }
                    return@exception
                }
                if (exception is ApiClientErrorException) {
                    val clientError = RestErrorFactory.createClientError(
                        exception.errorCode,
                        exception.msg,
                        exception.data
                    )
                    LOG.warn("Client error was caught, uniqueId:${clientError.uniqueId}", exception)
                    webResponse(call) {
                        JsonWebResponse.create(
                            body = clientError,
                            status = HttpStatusCode.BadRequest
                        )
                    }
                    return@exception
                }

                val serverError = RestErrorFactory.createServerError()
                LOG.error("Unhandled exception caught, uniqueId:${serverError.uniqueId}", exception)
                webResponse(call) {
                    JsonWebResponse.create(
                        body = serverError,
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
        }
    }
}
