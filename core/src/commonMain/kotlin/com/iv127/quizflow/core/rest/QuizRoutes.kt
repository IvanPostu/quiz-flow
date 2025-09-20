package com.iv127.quizflow.core.rest

import com.iv127.quizflow.core.utils.IOUtils
import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.http.ContentType
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.contentType
import io.ktor.server.request.receiveChannel
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

// TODO going to be removed
class QuizRoutes : ApiRoute {
    private val LOG = KtorSimpleLogger(getClassFullName(QuizRoutes::class))
    override fun setup(parent: Route) {
        parent.post("/upload") {
            val contentType = call.request.contentType()
            val boundary = contentType.parameter("boundary") ?: throw IllegalArgumentException("Boundary not found")

            println(boundary)

            val channel: ByteReadChannel = call.receiveChannel()
            val rawBody = channel.readRemaining().readByteArray()
            val multipartData = parseMultipart(rawBody, boundary)


            println("Received raw data: ${rawBody.decodeToString()}")
            call.respondText("Received raw body successfully.")
        }

        parent.post("/upload2") {
            val multipartData: MultiPartData
            try {
                multipartData = call.receiveMultipart()
            } catch (e: Exception) {
                LOG.error("Error happened", e)
                throw e
            }

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        // Handle form data if needed
                    }

                    is PartData.FileItem -> {
                        val fileBytes = part.provider().readRemaining().readByteArray()
                        val fileName = part.originalFileName as String

                        println(IOUtils.byteArrayToString(fileBytes))
                    }

                    else -> {}
                }
                part.dispose()
            }

            call.respondText("upload")
        }
    }
}


sealed class MultipartPart {
    data class FormField(val name: String, val value: String) : MultipartPart()
    data class FilePart(val name: String, val filename: String, val contentType: ContentType?, val content: ByteArray) :
        MultipartPart()
}

data class CustomMultipartData(val parts: List<MultipartPart>)

fun parseMultipart(byteArray: ByteArray, boundary: String): CustomMultipartData {
    val parts = mutableListOf<MultipartPart>()

    // Convert boundary to byte array for comparison
    val boundaryBytes = "--$boundary".toByteArray()
    val rawParts = byteArray.split(boundaryBytes)

    // Iterate over the parts and parse each one
    rawParts.forEach { rawPart ->
        if (rawPart.isNotEmpty()) {
            val part = parsePart(rawPart)
            part?.let { parts.add(it) }
        }
    }

    return CustomMultipartData(parts)
}

fun ByteArray.split(delimiter: ByteArray): List<ByteArray> {
    val result = mutableListOf<ByteArray>()
    var currentIndex = 0

    while (true) {
        val index = indexOf(delimiter, currentIndex)
        if (index == -1) break
        result.add(sliceArray(currentIndex until index))
        currentIndex = index + delimiter.size
    }
    result.add(sliceArray(currentIndex until size)) // Add remaining part
    return result
}

fun parsePart(rawPart: ByteArray): MultipartPart? {
    val partString = rawPart.decodeToString()
    val headersEndIndex = partString.indexOf("\r\n\r\n")

    if (headersEndIndex == -1) return null // Invalid part, skip

    // Extract headers (before "\r\n\r\n")
    val headers = partString.substring(0, headersEndIndex)
    val body = rawPart.sliceArray(headersEndIndex + 4 until rawPart.size)

    // Parse Content-Disposition header to get the name and filename
    val contentDisposition = headers.split("\r\n").firstOrNull { it.startsWith("Content-Disposition") }
    val contentType = headers.split("\r\n").firstOrNull { it.startsWith("Content-Type") }

    val dispositionParts = contentDisposition?.split(";")?.map { it.trim() } ?: emptyList()

    val name = dispositionParts.find { it.startsWith("name=") }?.substringAfter("=")?.trim('"')
    val filename = dispositionParts.find { it.startsWith("filename=") }?.substringAfter("=")?.trim('"')

    return if (filename != null) {
        // This is a file part
        val contentTypeHeader = contentType?.substringAfter("Content-Type: ")?.trim()
        val contentTypeObj = contentTypeHeader?.let { ContentType.parse(it) }
        MultipartPart.FilePart(name ?: "", filename, contentTypeObj, body)
    } else {
        // This is a form field
        MultipartPart.FormField(name ?: "", body.decodeToString())
    }
}

fun ByteArray.indexOf(delimiter: ByteArray, fromIndex: Int = 0): Int {
    // Ensure the delimiter isn't longer than the array
    if (delimiter.size > this.size) return -1

    // Iterate over the array starting from the `fromIndex`
    for (i in fromIndex..this.size - delimiter.size) {
        // Compare the current slice of the array with the delimiter
        var match = true
        for (j in delimiter.indices) {
            if (this[i + j] != delimiter[j]) {
                match = false
                break
            }
        }

        // If the whole delimiter matches, return the current index
        if (match) return i
    }

    // If no match is found, return -1
    return -1
}
