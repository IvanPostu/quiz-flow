package com.iv127.quizflow.core.platform.proc

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.posix.readlink

actual class ProcessUtils {

    companion object {
        private const val BUFFER_SIZE = 8 * 1024
    }

    actual fun getPathToExecutable(): String {
        return internalGetPathToExecutable()
    }

    actual fun getPathToExecutableDirectory(): String {
        return internalGetPathToExecutable()
            .replaceAfterLast("/", "")
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun internalGetPathToExecutable(): String {
        val buffer = ByteArray(BUFFER_SIZE)
        val len = readlink("/proc/self/exe", buffer.refTo(0), buffer.size.toLong().toULong())
        if (len != -1L) {
            return buffer.decodeToString().substring(0, len.toInt())
        }
        throw IllegalStateException("Can't get current executable path")
    }
}
