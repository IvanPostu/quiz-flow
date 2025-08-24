package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.model.quiz.question.file.FileIO
import com.iv127.quizflow.core.startQuizFlowApplication
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.SIGINT
import platform.posix.signal
import platform.posix.sleep

private val isShutdown: AtomicBoolean = atomic(false)

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    val q = FileIO().readAll("/home/iv127/Projects/quiz-flow/webapp/src/nativeMain/resources/a.txt")
    println(byteArrayToStringUsingBuffer(q.first()))

    val serverApp = startQuizFlowApplication(args)

    signal(SIGINT, staticCFunction { signal: Int ->
        isShutdown.value = true
    })

    while (!isShutdown.value) {
        sleep(1U)
    }
    serverApp.stop(10_000, 10_000)
}

private fun byteArrayToStringUsingBuffer(byteArray: ByteArray): String {
    val stringBuilder = StringBuilder(byteArray.size)
    for (byte in byteArray) {
        stringBuilder.append(byte.toInt().toChar())
    }
    return stringBuilder.toString()
}

