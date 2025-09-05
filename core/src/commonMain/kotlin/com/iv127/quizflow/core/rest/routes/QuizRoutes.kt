package com.iv127.quizflow.core.rest.routes

import com.iv127.quizflow.core.platform.io.IOUtils
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

class QuizRoutes : ApiRoute {
    override fun setup(parent: Route) {
        parent.post("/upload") {
            val multipartData = call.receiveMultipart()

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
