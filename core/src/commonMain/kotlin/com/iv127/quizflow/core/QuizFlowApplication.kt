package com.iv127.quizflow.core


import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing


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


fun Application.quizFlowApplicationModule() {
    routing {
        get("/") {
            val abc: Abc = Abc()
            call.respondText(abc.test())
        }
    }
}

interface QuizFlowApplication {
    fun stop(
        gracePeriodMillis: Long,
        timeoutMillis: Long
    )
}
