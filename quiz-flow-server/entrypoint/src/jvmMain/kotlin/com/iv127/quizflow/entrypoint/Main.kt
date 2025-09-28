package com.iv127.quizflow.entrypoint

import com.iv127.quizflow.core.Application
import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.PlatformProcess
import com.iv127.quizflow.core.resource.Resource
import com.iv127.quizflow.core.utils.IOUtils

fun main(args: Array<String>) {
    println(PlatformProcess().getPathToExecutableDirectory())
    val serverApp = Application.startQuizFlowApplication(args, PlatformServicesImpl())

    val resourceUtils = Resource(FileIO(), PlatformProcess())
    println(IOUtils.byteArrayToString(resourceUtils.readResource("a.txt")))
    println(IOUtils.byteArrayToString(resourceUtils.readResource("abc.json")))
    Runtime.getRuntime().addShutdownHook(Thread {
        serverApp.stop(1000, 5000)
    })
    Thread.currentThread().join()
}
