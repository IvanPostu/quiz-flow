package com.iv127.quizflow.core.server

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.withCharset
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray

class KtorJsonWebResponse {
    companion object {
        fun create(
            body: String,
            status: HttpStatusCode = HttpStatusCode.OK
        ): OutgoingContent.ByteArrayContent {
            return object : OutgoingContent.ByteArrayContent() {
                override val status: HttpStatusCode
                    get() = status

                override val contentType: ContentType =
                    ContentType.Application.Json.withCharset(Charsets.UTF_8)

                override fun bytes(): ByteArray {
                    return body.toByteArray(Charsets.UTF_8)
                }
            }
        }
    }
}
