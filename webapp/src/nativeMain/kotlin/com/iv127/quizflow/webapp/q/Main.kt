package com.iv127.quizflow.webapp.q

import com.iv127.quizflow.core.Application
import com.iv127.quizflow.core.model.question.ResourceUtils
import com.iv127.quizflow.core.platform.PlatformServices
import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.ProcessUtils
import com.iv127.quizflow.core.utils.IOUtils
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
    println(ProcessUtils().getPathToExecutable())
    println(ProcessUtils().getPathToExecutableDirectory())

    val resourceUtils = ResourceUtils(FileIO(), ProcessUtils())
    println(IOUtils.byteArrayToString(resourceUtils.readResource("/a.txt")))
    println(IOUtils.byteArrayToString(resourceUtils.readResource("/abc.json")))

    val serverApp = Application.startQuizFlowApplication(args, object : PlatformServices {
        override fun getProcessUtils(): ProcessUtils {
            return ProcessUtils()
        }

        override fun getFileIO(): FileIO {
            return FileIO()
        }
    })

    signal(SIGINT, staticCFunction { signal: Int ->
        isShutdown.value = true
    })

    while (!isShutdown.value) {
        sleep(1U)
    }
    serverApp.stop(10_000, 10_000)
}



