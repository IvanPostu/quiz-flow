package com.iv127.quizflow.core.platform.proc

import java.io.File
import java.net.URL


actual class PlatformProcess {
    actual fun getPathToExecutable(): String {
        return internalGetPathToExecutable()
    }

    actual fun getPathToExecutableDirectory(): String {
        return internalGetPathToExecutable()
            .replaceAfterLast("/", "")
    }

    private fun internalGetPathToExecutable(): String {
        val location: URL = PlatformProcess::class.java.getProtectionDomain().codeSource.location
        val file = File(location.path)
        return file.absolutePath
    }

    actual fun runShellScriptAndGetOutput(scriptContent: String): ProcessExecutionResult {
        val (returnCode, out) = internalRunShellScriptAndGetOutput(scriptContent)
        return ProcessExecutionResult(returnCode, out)
    }

    private fun internalRunShellScriptAndGetOutput(
        scriptContent: String,
    ): Pair<Int, String> {

        try {
            val processBuilder = ProcessBuilder("sh", "-c", scriptContent)
            processBuilder.redirectErrorStream(true)

            val process = processBuilder.start()
            val out = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            return Pair(exitCode, out)
        } catch (e: Exception) {
            throw IllegalStateException("Error executing command: ${e.message}", e)
        }
    }
}
