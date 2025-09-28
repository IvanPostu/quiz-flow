package com.iv127.quizflow.core

import com.iv127.quizflow.core.platform.file.FileIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.PipelineCall
import io.ktor.server.application.call
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import io.ktor.util.pipeline.PipelineContext
import kotlinx.io.files.Path

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

        val fileFullPath = Path(
            staticFilesDirectory + requestUri
                .replace(pathPrefix, "")
                .replace("/", fileIo.getPathSeparator())
                .replace("..", "")
        )
        try {
            // TODO: static files are being loaded in memory and returned, which is wrong, use ByteStreams instead
            val fileBytes = fileIo.readAll(fileFullPath.toString())
            context.call.respondBytes(fileBytes)
        } catch (e: Exception) {
            logger.warn("Can't read file: $fileFullPath due to: ${e.message}")
            context.call.respond(status = HttpStatusCode.NotFound, "Resource not found!")
        }
    }

    fun getIndexHtmlStaticFileOrElse(fallbackTextIfMissing: String): ByteArray {
        val fileFullPath = Path(staticFilesDirectory + fileIo.getPathSeparator() + "index.html")
        try {
            // TODO: static files are being loaded in memory and returned, which is wrong, use ByteStreams instead
            val fileBytes = fileIo.readAll(fileFullPath.toString())
            return fileBytes
        } catch (e: Exception) {
            logger.error("Can't read file: $fileFullPath", e)
            return fallbackTextIfMissing.encodeToByteArray()
        }
    }
}
