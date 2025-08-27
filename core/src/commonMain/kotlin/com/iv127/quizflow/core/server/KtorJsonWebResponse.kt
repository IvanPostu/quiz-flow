package com.iv127.quizflow.core.server

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.withCharset
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.json.Json

class KtorJsonWebResponse(
    val body: Any?,
    override val status: HttpStatusCode = HttpStatusCode.OK,
) : OutgoingContent.ByteArrayContent() {
    override val contentType: ContentType =
        ContentType.Application.Json.withCharset(Charsets.UTF_8)

    override fun bytes(): ByteArray {
        return Json.encodeToString(body)
            .toByteArray(Charsets.UTF_8)
    }
}
