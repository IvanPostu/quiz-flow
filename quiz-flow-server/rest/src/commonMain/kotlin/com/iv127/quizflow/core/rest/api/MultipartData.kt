package com.iv127.quizflow.core.rest.api

import io.ktor.http.ContentType


sealed class MultipartData {
    data class FormField(val name: String, val value: String) : MultipartData()
    data class FilePart(
        val name: String,
        val filename: String,
        val contentType: ContentType?,
        val content: ByteArray
    ) : MultipartData()
}
