package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.Application
import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.PlatformProcess
import com.iv127.quizflow.core.resource.Resource
import com.iv127.quizflow.core.utils.IOUtils
import kotlin.concurrent.AtomicReference
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.SIGINT
import platform.posix.signal
import platform.posix.usleep

private val appRef: AtomicReference<Application.Companion.QuizFlowApplication?> = AtomicReference(null)

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    println(PlatformProcess().getPathToExecutable())
    println(PlatformProcess().getPathToExecutableDirectory())

    val resourceUtils = Resource(FileIO(), PlatformProcess())
    println(IOUtils.byteArrayToString(resourceUtils.readResource("a.txt")))
    println(IOUtils.byteArrayToString(resourceUtils.readResource("abc.json")))

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



