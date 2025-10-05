package com.iv127.quizflow.core.ktor

import com.iv127.quizflow.core.rest.api.MultipartData
import io.ktor.http.ContentType
import io.ktor.utils.io.core.toByteArray

class Multipart {
    companion object {

        // replacement for: ktor's call.receiveMultipart() which doesn't work on linux_x64
        fun parseMultipart(byteArray: ByteArray, boundary: String): List<MultipartData> {
            val parts = mutableListOf<MultipartData>()

            val boundaryBytes = "--$boundary".toByteArray()
            val rawParts = byteArray.split(boundaryBytes)

            rawParts.forEach { rawPart ->
                if (rawPart.isNotEmpty()) {
                    val part = parsePart(rawPart)
                    part?.let { parts.add(it) }
                }
            }
            return parts
        }

        private fun ByteArray.split(delimiter: ByteArray): List<ByteArray> {
            val result = mutableListOf<ByteArray>()
            var currentIndex = 0

            while (true) {
                val index = indexOf(delimiter, currentIndex)
                if (index == -1) break
                result.add(sliceArray(currentIndex until index))
                currentIndex = index + delimiter.size
            }
            result.add(sliceArray(currentIndex until size))
            return result
        }

        private fun parsePart(rawPart: ByteArray): MultipartData? {
            val partString = rawPart.decodeToString()
            val headersEndIndex = partString.indexOf("\r\n\r\n")

            if (headersEndIndex == -1) return null // Invalid part, skip

            val headers = partString.substring(0, headersEndIndex)
            val body = rawPart.sliceArray(headersEndIndex + 4 until rawPart.size - 2)

            val contentDisposition = headers.split("\r\n").firstOrNull { it.startsWith("Content-Disposition") }
            val contentType = headers.split("\r\n").firstOrNull { it.startsWith("Content-Type") }

            val dispositionParts = contentDisposition?.split(";")?.map { it.trim() } ?: emptyList()

            val name = dispositionParts.find { it.startsWith("name=") }?.substringAfter("=")?.trim('"')
            val filename = dispositionParts.find { it.startsWith("filename=") }?.substringAfter("=")?.trim('"')

            return if (filename != null) {
                val contentTypeHeader = contentType?.substringAfter("Content-Type: ")?.trim()
                val contentTypeObj = contentTypeHeader?.let { ContentType.parse(it) }
                MultipartData.FilePart(name ?: "", filename, body, contentTypeObj)
            } else {
                MultipartData.FormField(name ?: "", body.decodeToString())
            }
        }

        private fun ByteArray.indexOf(delimiter: ByteArray, fromIndex: Int = 0): Int {
            if (delimiter.size > this.size) return -1

            for (i in fromIndex..this.size - delimiter.size) {
                var match = true
                for (j in delimiter.indices) {
                    if (this[i + j] != delimiter[j]) {
                        match = false
                        break
                    }
                }
                if (match) return i
            }
            return -1
        }

    }
}
