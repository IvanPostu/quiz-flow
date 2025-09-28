package com.iv127.quizflow.entrypoint

import com.iv127.quizflow.core.Application

fun main(args: Array<String>) {
    val serverApp = Application.startQuizFlowApplication(args, PlatformServicesImpl())
    Runtime.getRuntime().addShutdownHook(Thread {
        serverApp.stop(1000, 5000)
    })
    Thread.currentThread().join()
}
