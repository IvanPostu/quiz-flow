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
import java.io.ByteArrayOutputStream
import java.io.InputStream

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

    fun readResourceAsByteArray(fileName: String): ByteArray {
        val classLoader = this::class.java.classLoader
        val inputStream: InputStream? = classLoader.getResourceAsStream(fileName)

        if (inputStream != null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            inputStream.use { stream ->
                stream.copyTo(byteArrayOutputStream)
            }
            return byteArrayOutputStream.toByteArray()
        } else {
            throw IllegalArgumentException("File not found in resources: $fileName")
        }
    }

    private suspend fun remapExceptionIfPossibleAndThrow(response: HttpResponse) {
        if (isSuccessStatusCode(response.status.value)) {
            return
        }
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


    private fun isSuccessStatusCode(statusCode: Int): Boolean {
        return statusCode in 200..299
    }

}
