package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.QuizFlowApplication
import com.iv127.quizflow.core.model.quiz.question.proc.ProcessUtils
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import sun.misc.Signal
import sun.misc.SignalHandler

@OptIn(ExperimentalAtomicApi::class)
fun main(args: Array<String>) {
    println(ProcessUtils().getPathToExecutable())
    println(ProcessUtils().getPathToExecutableDirectory())
    val serverApp = QuizFlowApplication.startQuizFlowApplication(args)
    val isShutdown = AtomicBoolean(false)

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
