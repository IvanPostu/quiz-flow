package com.iv127.quizflow.core.platform.proc

import java.io.File
import java.net.URL


actual class PlatformProcess {

    actual fun getPathToExecutableDirectory(): String {
        val debugApplicationRootFolder = runShellScriptAndGetOutput("echo -n \$DEBUG_APPLICATION_ROOT_FOLDER").output
        if (debugApplicationRootFolder.isNotBlank()) {
            return debugApplicationRootFolder
        }
        val result = internalGetPathToExecutable()
            .replaceAfterLast("/", "")
        return if (result.endsWith("/")) result.substring(0, result.length - 1) else result
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
