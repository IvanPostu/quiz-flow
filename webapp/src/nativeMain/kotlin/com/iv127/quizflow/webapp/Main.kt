package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.Application
import com.iv127.quizflow.core.model.question.ResourceUtils
import com.iv127.quizflow.core.platform.PlatformServices
import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.ProcessUtils
import com.iv127.quizflow.core.utils.IOUtils
import kotlin.concurrent.AtomicReference
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.staticCFunction
import libsimple.sayHello
import platform.posix.SIGINT
import platform.posix.signal
import platform.posix.sleep

private val isShutdown: AtomicBoolean = atomic(false)
private val appRef: AtomicReference<Application.Companion.QuizFlowApplication?> = AtomicReference(null)

@OptIn(ExperimentalForeignApi::class)
fun test() {
    println("Hello from Kotlin Native!")
    memScoped {
        val name = "Kotlin".cstr.getPointer(this) // Convert Kotlin string to C string
        sayHello(name)
    }

    val ksqlite = KSqlite("/home/iv127/Projects/quiz-flow/test.db")
    try {
        ksqlite.execute(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT NOT NULL
            );
        """.trimIndent()
        ) { cols, data ->
            cols.forEach { print(it + " ") }
            println()
            data.forEach { print(it + " ") }
            println()
            0
        }

        ksqlite.execute(
            """
        INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com');
        INSERT INTO users (name, email) VALUES ('Bob', 'bob@example.com');
        INSERT INTO users (name, email) VALUES ('Charlie', 'charlie@example.com');
        """.trimIndent()
        ) { cols, data ->
            cols.forEach { print(it + " ") }
            println()
            data.forEach { print(it + " ") }
            println()
            0
        }

        ksqlite.execute(
            """
        SELECT * FROM users;
        """.trimIndent()
        ) { cols, data ->
            cols.forEach { print(it + " ") }
            println()
            data.forEach { print(it + " ") }
            println()
            0
        }
    } finally {
        ksqlite.close()
    }
}


@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
//    test()
//    return;
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



