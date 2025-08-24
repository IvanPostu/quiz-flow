package com.iv127.quizflow.core.model.quiz.question.proc

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.posix.readlink

actual class ProcessUtils {
    @OptIn(ExperimentalForeignApi::class)
    actual fun getExecutablePath(): String {
        val buffer = ByteArray(1024)
        val len = readlink("/proc/self/exe", buffer.refTo(0), buffer.size.toLong().toULong())
        if (len != -1L) {
            val path = buffer.decodeToString().substring(0, len.toInt())
            return path.replaceAfterLast("/", "")
        }
        throw IllegalStateException("can't get current executable path")
    }
}
