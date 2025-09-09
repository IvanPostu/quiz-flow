package com.iv127.quizflow.core.platform.proc

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen
import platform.posix.readlink

actual class PlatformProcess {

    companion object {
        private const val BUFFER_SIZE = 8 * 1024
    }

    actual fun getPathToExecutableDirectory(): String {
        val result = internalGetPathToExecutable()
            .replaceAfterLast("/", "")
        return if (result.endsWith("/")) result.substring(0, result.length - 1) else result
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
        val commandToExecute = "$scriptContent 2>&1"
        val fp = popen(commandToExecute, "r") ?: throw IllegalStateException("Failed to run command: $scriptContent")

        val stdout = buildString {
            val buffer = ByteArray(4096)
            while (true) {
                val input = fgets(buffer.refTo(0), buffer.size, fp) ?: break
                append(input.toKString())
            }
        }

        val status = pclose(fp)
        return ProcessExecutionResult(status, stdout)
    }
}
