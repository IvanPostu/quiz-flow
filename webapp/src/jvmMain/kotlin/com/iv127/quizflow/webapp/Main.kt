package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.Application
import com.iv127.quizflow.core.model.question.ResourceUtils
import com.iv127.quizflow.core.platform.PlatformServices
import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.PlatformProcess
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.utils.IOUtils

fun main(args: Array<String>) {
    println(PlatformProcess().getPathToExecutable())
    println(PlatformProcess().getPathToExecutableDirectory())
    val serverApp = Application.startQuizFlowApplication(args, object : PlatformServices {
        override fun getProcessUtils(): PlatformProcess {
            return PlatformProcess()
        }

        override fun getFileIO(): FileIO {
            return FileIO()
        }

        override fun close() {
        }

        override fun createSqliteDatabase(path: String): SqliteDatabase {
            TODO("Not yet implemented")
        }
    })

    val resourceUtils = ResourceUtils(FileIO(), PlatformProcess())
    println(IOUtils.byteArrayToString(resourceUtils.readResource("/a.txt")))
    println(IOUtils.byteArrayToString(resourceUtils.readResource("/abc.json")))
    Runtime.getRuntime().addShutdownHook(Thread {
        serverApp.stop(1000, 5000)
    })
    Thread.currentThread().join()
}
