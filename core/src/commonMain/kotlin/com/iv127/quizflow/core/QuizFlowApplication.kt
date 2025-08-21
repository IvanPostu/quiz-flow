package com.iv127.quizflow.core


import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing


fun runQuizFlowApplication(args: Array<String>) {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::quizFlowApplicationModule)
        .start(wait = true)
}


fun Application.quizFlowApplicationModule() {
    routing {
        get("/") {
            val abc: Abc = Abc()
            call.respondText(abc.test())
        }
    }
}
