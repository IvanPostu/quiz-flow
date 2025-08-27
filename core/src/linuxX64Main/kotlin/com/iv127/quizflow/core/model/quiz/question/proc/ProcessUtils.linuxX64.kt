package com.iv127.quizflow.core.model.quiz.question.proc

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.posix.readlink

actual class ProcessUtils {

    actual fun getPathToExecutable(): String {
        return internalGetPathToExecutable()
    }

    actual fun getPathToExecutableDirectory(): String {
        return internalGetPathToExecutable()
            .replaceAfterLast("/", "")
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun internalGetPathToExecutable(): String {
        val bufferSize = 8 * 1024
        val buffer = ByteArray(bufferSize)
        val len = readlink("/proc/self/exe", buffer.refTo(0), buffer.size.toLong().toULong())
        if (len != -1L) {
            return buffer.decodeToString().substring(0, len.toInt())
        }
        throw IllegalStateException("Can't get current executable path")
    }
}
