package com.iv127.quizflow.core.platform.proc

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.popen
import platform.posix.readlink

actual class PlatformProcess {

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

    @OptIn(ExperimentalForeignApi::class)
    actual fun runShellScriptAndGetOutput(scriptContent: String): ProcessExecutionResult {
        val pipe: CPointer<FILE> =
            popen(scriptContent, "r") ?: throw IllegalStateException("Failed to execute command")

        try {
            val output = StringBuilder()
            val buffer = ByteArray(BUFFER_SIZE)

            while (true) {
                val line = fgets(buffer.refTo(0), buffer.size, pipe) ?: break
                output.append(line.toKString())
            }
            return ProcessExecutionResult(0, output.toString(), "")
        } finally {
            fclose(pipe)
        }
    }
}
