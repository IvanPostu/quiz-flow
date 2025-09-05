package com.iv127.quizflow.core

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.popen

// TODO: To expose for all platforms
class Process {
    @OptIn(ExperimentalForeignApi::class)
    fun runShellScriptAndGetOutput(script: String): String {
        val pipe: CPointer<FILE> =
            popen(script, "r") ?: throw Exception("Failed to execute command")  // "r" means read mode

        val output = StringBuilder()
        val buffer = ByteArray(1024)

        while (true) {
            val line = fgets(buffer.refTo(0), buffer.size, pipe)
            if (line == null) {
                break
            }
            output.append(line.toKString()) // Convert each line to a string and append
        }

        fclose(pipe)  // Close the pipe when done
        return output.toString()  // Return the full output as a single string
    }
}
