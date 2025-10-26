package com.iv127.quizflow.core.server

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json

data class JsonWebResponse(
    val body: String,
    override val statusCode: Int = 200,
    override val headers: Map<String, List<String>> = mapOf(),
) : WebResponse() {

    companion object {
        inline fun <reified T> create(
            body: T,
            status: HttpStatusCode = HttpStatusCode.OK,
            headers: Map<String, List<String>> = mapOf()
        ): JsonWebResponse {
            val serialized = Json.encodeToString(body)
            return JsonWebResponse(serialized, status.value, headers)
        }

        inline fun empty(
            status: HttpStatusCode = HttpStatusCode.OK,
            headers: Map<String, List<String>> = mapOf()
        ): JsonWebResponse {
            val serialized = Json.encodeToString(mapOf<String, String>())
            return JsonWebResponse(serialized, status.value, headers)
        }
    }

    override fun copyResponse(
        statusCode: Int,
        headers: Map<String, List<String>>,
    ): WebResponse = copy(body, statusCode, headers)
}
