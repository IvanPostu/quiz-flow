package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.Application
import com.iv127.quizflow.core.model.question.ResourceUtils
import com.iv127.quizflow.core.platform.PlatformServices
import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.ProcessUtils
import com.iv127.quizflow.core.utils.IOUtils

fun main(args: Array<String>) {
    println(ProcessUtils().getPathToExecutable())
    println(ProcessUtils().getPathToExecutableDirectory())
    val serverApp = Application.startQuizFlowApplication(args, object : PlatformServices {
        override fun getProcessUtils(): ProcessUtils {
            return ProcessUtils()
        }

        override fun getFileIO(): FileIO {
            return FileIO()
        }

    })

    val resourceUtils = ResourceUtils(FileIO(), ProcessUtils())
    println(IOUtils.byteArrayToString(resourceUtils.readResource("/a.txt")))
    println(IOUtils.byteArrayToString(resourceUtils.readResource("/abc.json")))
    Runtime.getRuntime().addShutdownHook(Thread {
        serverApp.stop(1000, 5000)
    })
    Thread.currentThread().join()
}
