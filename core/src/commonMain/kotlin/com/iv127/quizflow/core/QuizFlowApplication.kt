package com.iv127.quizflow.core

import com.iv127.quizflow.core.model.quiz.question.file.FileIO
import com.iv127.quizflow.core.model.quiz.question.proc.ProcessUtils
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.routing.get
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

        // TODO handler that any undefined request redirects to index.html - for SPA
        // e.g.:
        //     @GetMapping("/{path:^(?!api|static|assets|images|favicon).*}")
        //    public String redirectToIndex() {
        //        return "forward:/index.html";
        //    }
        routing {
            get(
                "/api/health-check",
                webResponse {
                    JsonWebResponse.create(HealthCheckResponse("SUCCESS"))
                }
            )
            get("/api/err") {
                throw IllegalStateException("Custom error")
            }
        }
    }
}

@Serializable
data class HealthCheckResponse(@SerialName("status") private val status: String)
