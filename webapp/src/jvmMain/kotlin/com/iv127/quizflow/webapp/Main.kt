package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.QuizFlowApplication
import com.iv127.quizflow.core.model.quiz.question.ResourceUtils
import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.io.IOUtils
import com.iv127.quizflow.core.platform.proc.ProcessUtils
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import sun.misc.Signal
import sun.misc.SignalHandler

@OptIn(ExperimentalAtomicApi::class)
fun main(args: Array<String>) {
    println(ProcessUtils().getPathToExecutable())
    println(ProcessUtils().getPathToExecutableDirectory())
    val serverApp = QuizFlowApplication.startQuizFlowApplication(args, FileIO(), ProcessUtils())
    val isShutdown = AtomicBoolean(false)

    val resourceUtils = ResourceUtils(FileIO(), ProcessUtils())
    println(IOUtils.byteArrayToString(resourceUtils.readResource("/a.txt")))
    println(IOUtils.byteArrayToString(resourceUtils.readResource("/abc.json")))

    Signal.handle(Signal("INT"), object : SignalHandler {
        override fun handle(signal: Signal?) {
            isShutdown.store(true)
        }
    })

    while (!isShutdown.load()) {
        Thread.sleep(1000)
    }

    serverApp.stop(10_000, 10_000)
}
