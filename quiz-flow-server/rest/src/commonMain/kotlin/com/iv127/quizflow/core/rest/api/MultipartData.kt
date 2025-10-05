package com.iv127.quizflow.core.rest.api


sealed class MultipartData {
    data class FormField(val name: String, val value: String) : MultipartData()
    data class FilePart(
        val name: String,
        val filename: String,
        val content: ByteArray
    ) : MultipartData() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as FilePart

            if (name != other.name) return false
            if (filename != other.filename) return false
            if (!content.contentEquals(other.content)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + filename.hashCode()
            result = 31 * result + content.contentHashCode()
            return result
        }
    }
}
