package com.iv127.quizflow.core.rest

import com.iv127.quizflow.core.ktor.Multipart
import com.iv127.quizflow.core.ktor.MultipartPart
import com.iv127.quizflow.core.utils.IOUtils
import com.iv127.quizflow.core.utils.getClassFullName
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
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

// TODO going to be removed
class QuizRoutes : ApiRoute {
    private val LOG = KtorSimpleLogger(getClassFullName(QuizRoutes::class))
    override fun setup(parent: Route) {
        parent.post("/upload") {
            val contentType = call.request.contentType()
            val boundary = contentType.parameter("boundary") ?: throw IllegalArgumentException("Boundary not found")
            val channel: ByteReadChannel = call.receiveChannel()
            val rawBody = channel.readRemaining().readByteArray()
            val multipartData = Multipart.parseMultipart(rawBody, boundary)

            for (part in multipartData.parts) {
                when (part) {
                    is MultipartPart.FormField -> {

                    }

                    is MultipartPart.FilePart -> {

                    }
                }
            }

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
