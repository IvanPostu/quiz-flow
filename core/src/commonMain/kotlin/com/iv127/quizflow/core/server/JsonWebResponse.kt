package com.iv127.quizflow.core.server

data class JsonWebResponse(
    val body: Any?,
    override val statusCode: Int = 200,
    override val headers: Map<String, List<String>> = mapOf(),
) : WebResponse() {
    override fun copyResponse(
        statusCode: Int,
        headers: Map<String, List<String>>,
    ): WebResponse = copy(body, statusCode, headers)
}
