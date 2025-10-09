package com.iv127.quizflow.core.rest.api

import io.ktor.http.ContentType


sealed class MultipartData {
    data class FormField(val name: String, val value: String) : MultipartData()
    data class FilePart(
        val name: String,
        val filename: String,
        val content: ByteArray,
        val contentType: ContentType?
    ) : MultipartData() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as FilePart

            if (name != other.name) return false
            if (filename != other.filename) return false
            if (!content.contentEquals(other.content)) return false
            if (contentType != other.contentType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + filename.hashCode()
            result = 31 * result + content.contentHashCode()
            result = 31 * result + (contentType?.hashCode() ?: 0)
            return result
        }

    }
}
