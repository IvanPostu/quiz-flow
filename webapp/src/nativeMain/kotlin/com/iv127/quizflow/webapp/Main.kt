package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.startQuizFlowApplication
import com.iv127.quizflow.webapp.file.FileIO
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.SIGINT
import platform.posix.signal
import platform.posix.sleep


@OptIn(ExperimentalAtomicApi::class)
fun main(args: Array<String>) {
    val q = FileIO().readAll("/home/iv127/Projects/quiz-flow/webapp/src/nativeMain/resources/a.txt")
    println(byteArrayToStringUsingBuffer(q.first()))

    val serverApp = startQuizFlowApplication(args)

    val isShutdown = AtomicBoolean(false)
    registerSigintHandler {
        serverApp.stop(5000, 10000)
        isShutdown.store(true)
    }

    while (!isShutdown.load()) {
        sleep(1U)
        println(1)
    }

    println(isShutdown)
}

fun byteArrayToStringUsingBuffer(byteArray: ByteArray): String {
    val stringBuilder = StringBuilder(byteArray.size)
    for (byte in byteArray) {
        stringBuilder.append(byte.toInt().toChar())
    }
    return stringBuilder.toString()
}

@OptIn(ExperimentalForeignApi::class)
private fun registerSigintHandler(func: () -> Unit) {
    signal(SIGINT, staticCFunction { signal: Int ->
        func()
    })
}
