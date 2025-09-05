package com.iv127.quizflow.core

import com.iv127.quizflow.core.platform.PlatformServices
import com.iv127.quizflow.core.rest.routes.ApiRoute
import com.iv127.quizflow.core.rest.routes.HealthCheckRoutes
import com.iv127.quizflow.core.rest.routes.QuizRoutes
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

class QuizFlowApplication {
    companion object {
        fun startQuizFlowApplication(
            args: Array<String>,
            platformServices: PlatformServices
        ): QuizFlowApplication {
            val embeddedServer =
                embeddedServer(
                    CIO,
                    port = 8080,
                    host = "0.0.0.0",
                    module = createApplicationModule(platformServices)
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

private val LOG: Logger = KtorSimpleLogger(QuizFlowApplication.toString())

fun createApplicationModule(platformServices: PlatformServices): Application.() -> Unit {
    val routeInstances = listOf<ApiRoute>(
        HealthCheckRoutes(),
        QuizRoutes(),
    )
    val processUtils = platformServices.getProcessUtils()
    val fileIo = platformServices.getFileIO()
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
            route("/api") {
                routeInstances.forEach { it.setup(this) }
            }

            get("/") {
                call.respondBytes(staticFilesProviderPlugin.getIndexHtmlStaticFileOrElse("Index html is missing!"))
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
