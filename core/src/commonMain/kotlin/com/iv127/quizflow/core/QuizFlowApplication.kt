package com.iv127.quizflow.core

import com.iv127.quizflow.core.model.quiz.question.file.FileIO
import com.iv127.quizflow.core.model.quiz.question.proc.ProcessUtils
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.TextWebResponse
import com.iv127.quizflow.core.server.webResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
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

val LOGGER: Logger = KtorSimpleLogger(QuizFlowApplication.toString())

fun createApplicationModule(fileIo: FileIO, processUtils: ProcessUtils): Application.() -> Unit {
    val pathToPublicDirectory = processUtils.getPathToExecutableDirectory() + "public"
    val staticFilesProviderPlugin = StaticFilesProvider(fileIo, "/public", pathToPublicDirectory)
    val requestTracePlugin = createRouteScopedPlugin("RequestTracePlugin", { }) {
        onCall { call ->
            LOGGER.info("${call.request.httpMethod}: ${call.request.uri}")
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
                LOGGER.error("Unhandled exception: ", exception)
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

@Serializable
data class HealthCheckResponse(@SerialName("status") private val status: String)
