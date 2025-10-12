package com.iv127.quizflow.core

import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.http.ContentType
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

    companion object {
        private val LOG: Logger = KtorSimpleLogger(getClassFullName(StaticFilesProvider::class))
        private val MIME_TYPE_MAP = mapOf(
            "html" to "text/html",
            "css" to "text/css",
            "js" to "application/javascript",
            "json" to "application/json",
            "png" to "image/png",
            "jpg" to "image/jpeg",
            "jpeg" to "image/jpeg",
            "gif" to "image/gif",
            "svg" to "image/svg+xml",
            "txt" to "text/plain",
            "pdf" to "application/pdf"
        )

    }

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

        val extension = fileFullPath.toString().substringAfterLast('.', "")
        val mimeType = MIME_TYPE_MAP[extension] ?: "application/octet-stream"
        try {
            // TODO: static files are being loaded in memory and returned, which is wrong, use ByteStreams instead
            val fileBytes = fileIo.readAll(fileFullPath.toString())
            context.call.respondBytes(fileBytes, contentType = ContentType.parse(mimeType))
        } catch (e: Exception) {
            LOG.warn("Can't read file: $fileFullPath due to: ${e.message}")
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
            LOG.error("Can't read file: $fileFullPath", e)
            return fallbackTextIfMissing.encodeToByteArray()
        }
    }
}
