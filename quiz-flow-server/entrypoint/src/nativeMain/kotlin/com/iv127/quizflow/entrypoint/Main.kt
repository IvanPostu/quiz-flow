package com.iv127.quizflow.entrypoint

import com.iv127.quizflow.core.Application
import kotlin.concurrent.AtomicReference
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.SIGINT
import platform.posix.signal
import platform.posix.usleep

private val appRef: AtomicReference<Application.Companion.QuizFlowApplication?> = AtomicReference(null)

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    val serverApp = Application.startQuizFlowApplication(args, PlatformServicesImpl())
    appRef.getAndSet(serverApp)

    signal(SIGINT, staticCFunction { signal: Int ->
        if (appRef.value != null) {
            appRef.value?.stop(1000, 5000)
            appRef.getAndSet(null)
        }
    })

    while (appRef.value != null) {
        usleep(500_000u); // 0.5 seconds
    }
}



