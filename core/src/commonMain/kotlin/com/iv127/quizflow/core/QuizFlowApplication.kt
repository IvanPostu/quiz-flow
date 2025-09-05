package com.iv127.quizflow.core

import com.iv127.quizflow.core.model.quiz.question.file.FileIO
import com.iv127.quizflow.core.model.quiz.question.io.IOUtils
import com.iv127.quizflow.core.model.quiz.question.proc.ProcessUtils
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.TextWebResponse
import com.iv127.quizflow.core.server.webResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.uri
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class QuizFlowApplication {
    companion object {
        fun startQuizFlowApplication(
            args: Array<String>,
            fileIo: FileIO,
            processUtils: ProcessUtils
        ): QuizFlowApplication {
            val embeddedServer =
                embeddedServer(
                    CIO,
                    port = 8080,
                    host = "0.0.0.0",
                    module = createApplicationModule(fileIo, processUtils)
                )

            embeddedServer.start(wait = false)

            return object : QuizFlowApplication {
                override fun stop(gracePeriodMillis: Long, timeoutMillis: Long) {
                    embeddedServer.stop(gracePeriodMillis, timeoutMillis)
                }
            }
        }

        interface QuizFlowApplication {
            fun stop(
                gracePeriodMillis: Long,
                timeoutMillis: Long
            )
        }

    }
}

val LOG: Logger = KtorSimpleLogger(QuizFlowApplication.toString())

fun createApplicationModule(fileIo: FileIO, processUtils: ProcessUtils): Application.() -> Unit {
    val pathToPublicDirectory = processUtils.getPathToExecutableDirectory() + "public"
    val staticFilesProviderPlugin = StaticFilesProvider(fileIo, "/public", pathToPublicDirectory)
    val requestTracePlugin = createRouteScopedPlugin("RequestTracePlugin", { }) {
        onCall { call ->
            LOG.info("${call.request.httpMethod}: ${call.request.uri}")
        }
    }

    return {
        install(requestTracePlugin)
        intercept(ApplicationCallPipeline.Call) {
            staticFilesProviderPlugin.intercept(this)
        }

        // TODO: enable only for development
        install(StatusPages) {
            exception<Throwable> { call, exception ->
                LOG.error("Unhandled exception: ", exception)
                val stackTrace = exception.stackTraceToString()

                call.respond(
                    """
                |Error: ${exception.message}
                |Stack Trace:
                |$stackTrace
            """.trimMargin(),
                    typeInfo = null
                )
            }
        }

        routing {
            fileUploadRoute()
            get("/") {
                call.respondBytes(staticFilesProviderPlugin.getIndexHtmlStaticFileOrElse("Index html is missing!"))
            }
            get(
                "/api/health-check",
                webResponse {
                    JsonWebResponse.create(HealthCheckResponse("SUCCESS"))
                }
            )
            get("/test/test", webResponse {
                TextWebResponse("test")
            })
            get("/api/err") {
                throw IllegalStateException("Custom error")
            }
            route("/{...}") {
                handle {
                    val requestUri = call.request.uri
                    val match = Regex("^/(?<path>(?!api|test).*)$").find(requestUri)
                    if (match == null) {
                        call.respond(HttpStatusCode.NotFound, typeInfo = null)
                    } else {
                        call.respondRedirect("/", permanent = false)
                    }
                }
            }
        }
    }
}

fun Route.fileUploadRoute() {
    post("/upload") {
        val multipartData = call.receiveMultipart()

        var fileName = ""

        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    // Handle form data if needed
                }

                is PartData.FileItem -> {
                    val fileBytes = part.provider().readRemaining().readByteArray()
                    fileName = part.originalFileName as String

                    println(IOUtils.byteArrayToString(fileBytes))
                }

                else -> {}
            }
            part.dispose()
        }

        call.respondText("upload")
    }
}

@Serializable
data class HealthCheckResponse(@SerialName("status") private val status: String)
