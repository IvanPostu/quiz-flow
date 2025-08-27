package com.iv127.quizflow.core

import com.iv127.quizflow.core.model.quiz.question.file.FileIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.PipelineCall
import io.ktor.server.application.call
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import io.ktor.util.pipeline.PipelineContext

class StaticFilesProvider(
    private val fileIo: FileIO,
    private val pathPrefix: String,
    private val staticFilesDirectory: String
) {
    private val logger: Logger = KtorSimpleLogger("StaticFilesProvider")

    suspend fun intercept(context: PipelineContext<Unit, PipelineCall>) {
        val requestUri = context.call.request.uri
        if (!requestUri.startsWith(pathPrefix)) {
            return
        }

        val fileFullPath = staticFilesDirectory + requestUri
            .replace(pathPrefix, "")
            .replace("/", fileIo.getPathSeparator())
        try {
            val fileBytes = fileIo.readAll(fileFullPath)
            context.call.respondBytes(fileBytes)
        } catch (e: Exception) {
            logger.error("Can't read file: $fileFullPath", e)
            context.call.respond(status = HttpStatusCode.NotFound, "Resource not found!")
        }
    }
}
