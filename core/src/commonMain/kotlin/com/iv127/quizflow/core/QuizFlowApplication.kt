package com.iv127.quizflow.core

import io.ktor.server.application.Application
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger

class QuizFlowApplication {
    companion object {
        fun startQuizFlowApplication(args: Array<String>): QuizFlowApplication {
            val embeddedServer =
                embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::quizFlowApplicationModule)

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

fun Application.quizFlowApplicationModule() {
    val RequestTracePlugin = createRouteScopedPlugin("RequestTracePlugin", { }) {
        onCall { call ->
            LOGGER.trace("Processing call: ${call.request.uri}")
        }
    }
    install(RequestTracePlugin)
    routing {
        get("/") {
            val abc: Abc = Abc()
            LOGGER.info("test")
            call.respondText(abc.test())
        }
    }
}
