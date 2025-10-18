package com.iv127.quizflow.core.rest.api.cookie

import io.ktor.http.CookieEncoding
import io.ktor.util.date.GMTDate

data class CookieResponse(
    val name: String,
    val value: String,
    val encoding: CookieEncoding = CookieEncoding.URI_ENCODING,
    val maxAge: Int? = null,
    val expires: GMTDate? = null,
    val domain: String? = null,
    val path: String? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false,
    val extensions: Map<String, String?> = emptyMap()
)
