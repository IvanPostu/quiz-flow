package com.iv127.quizflow.server.acceptance.test.rest

import com.iv127.quizflow.core.rest.api.error.RestErrorResponse
import io.ktor.client.statement.HttpResponse

data class RestErrorException(
    val httpStatusCode: Int,
    val restErrorResponse: RestErrorResponse,
    val response: HttpResponse
) : Exception()
