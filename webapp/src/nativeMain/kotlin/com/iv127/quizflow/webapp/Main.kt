package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.Application
import com.iv127.quizflow.core.model.question.ResourceUtils
import com.iv127.quizflow.core.platform.PlatformServices
import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.PlatformProcess
import com.iv127.quizflow.core.utils.IOUtils
import kotlin.concurrent.AtomicReference
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.posix.SIGINT
import platform.posix.signal
import platform.posix.sleep

private val isShutdown: AtomicBoolean = atomic(false)
private val appRef: AtomicReference<Application.Companion.QuizFlowApplication?> = AtomicReference(null)

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    println(PlatformProcess().getPathToExecutable())
    println(PlatformProcess().getPathToExecutableDirectory())

    val resourceUtils = ResourceUtils(FileIO(), PlatformProcess())
    println(IOUtils.byteArrayToString(resourceUtils.readResource("/a.txt")))
    println(IOUtils.byteArrayToString(resourceUtils.readResource("/abc.json")))

    val serverApp = Application.startQuizFlowApplication(args, object : PlatformServices {
        override fun getProcessUtils(): PlatformProcess {
            return PlatformProcess()
        }

        override fun getFileIO(): FileIO {
            return FileIO()
        }

        override fun close() {
        }
    })
    appRef.getAndSet(serverApp)

    signal(SIGINT, staticCFunction { signal: Int ->
        if (!isShutdown.value) {
            appRef.value?.stop(1000, 5000)
            isShutdown.value = true
        }
    })

    while (!isShutdown.value) {
        sleep(1U)
    }
}



