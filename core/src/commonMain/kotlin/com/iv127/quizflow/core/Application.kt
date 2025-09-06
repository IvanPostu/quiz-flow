package com.iv127.quizflow.core

import com.iv127.quizflow.core.platform.PlatformServices
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer


class Application {
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

