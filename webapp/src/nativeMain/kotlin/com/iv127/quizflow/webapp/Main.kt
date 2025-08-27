package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.QuizFlowApplication
import com.iv127.quizflow.core.model.quiz.question.file.FileIO
import com.iv127.quizflow.core.model.quiz.question.file.FileIOUtils
import com.iv127.quizflow.core.model.quiz.question.proc.ProcessUtils
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
    val aTxtContent = FileIO().readAll(ProcessUtils().getPathToExecutableDirectory() + "/a.txt")
    println(FileIOUtils.byteListOfArraysToString(aTxtContent))

    println(ProcessUtils().getPathToExecutable())
    println(ProcessUtils().getPathToExecutableDirectory())

    val serverApp = QuizFlowApplication.startQuizFlowApplication(args)

    signal(SIGINT, staticCFunction { signal: Int ->
        isShutdown.value = true
    })

    while (!isShutdown.value) {
        sleep(1U)
    }
    serverApp.stop(10_000, 10_000)
}



