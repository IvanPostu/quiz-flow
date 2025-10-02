package com.iv127.quizflow.core

import com.iv127.quizflow.core.platform.PlatformServices
import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.util.logging.KtorSimpleLogger
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class Application {
    companion object {
        @OptIn(ExperimentalTime::class)
        fun startQuizFlowApplication(
            args: Array<String>,
            platformServices: PlatformServices
        ): QuizFlowApplication {
            val logger = KtorSimpleLogger(getClassFullName(Application::class))
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
                    val now = Clock.System.now().toEpochMilliseconds()
                    try {
                        logger.info("Application shutdown initialed")
                        embeddedServer.stop(gracePeriodMillis, timeoutMillis)
                    } finally {
                        logger.info(
                            "Application shutdown finished, time taken: ${
                                Clock.System.now().toEpochMilliseconds() - now
                            } milliseconds"
                        )
                    }
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

