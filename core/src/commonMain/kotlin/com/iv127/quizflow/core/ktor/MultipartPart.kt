package com.iv127.quizflow.core.ktor

import io.ktor.http.ContentType


sealed class MultipartPart {
    data class FormField(val name: String, val value: String) : MultipartPart()
    data class FilePart(
        val name: String,
        val filename: String,
        val contentType: ContentType?,
        val content: ByteArray
    ) : MultipartPart()
}
