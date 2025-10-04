package com.iv127.quizflow.server.acceptance.test

import com.iv127.quizflow.core.rest.api.error.RestErrorResponse
import com.iv127.quizflow.server.acceptance.test.rest.RestErrorException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json

data class GlobalConfig(val baseUrl: String = "http://localhost:8080") {
    companion object {
        private val CLIENT = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }
        val INSTANCE = GlobalConfig()
    }

    @Throws(RestErrorException::class)
    suspend fun performRequest(requestCallback: suspend (client: HttpClient) -> HttpResponse): HttpResponse {
        val response = requestCallback(CLIENT)
        remapExceptionIfPossibleAndThrow(response)
        return response
    }

    private suspend fun remapExceptionIfPossibleAndThrow(response: HttpResponse) {
        val restErrorResponse: RestErrorResponse
        try {
            restErrorResponse = response.body<RestErrorResponse>()
        } catch (e: Exception) {
            return
        }
        throw RestErrorException(
            httpStatusCode = response.status.value,
            restErrorResponse = restErrorResponse,
            response = response
        )
    }
}
