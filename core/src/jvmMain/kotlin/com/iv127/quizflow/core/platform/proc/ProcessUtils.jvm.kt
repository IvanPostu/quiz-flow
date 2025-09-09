package com.iv127.quizflow.core.platform.proc

import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.util.logging.KtorSimpleLogger
import java.io.File
import java.net.URL


actual class PlatformProcess {
    companion object {
        private val LOG = KtorSimpleLogger(getClassFullName(PlatformProcess::class))
    }

    actual fun getPathToExecutableDirectory(): String {
        val debugApplicationRootFolder = runShellScriptAndGetOutput("echo -n \$DEBUG_APPLICATION_ROOT_FOLDER").output
        if (debugApplicationRootFolder.isNotBlank()) {
            LOG.warn(
                "Env variable DEBUG_APPLICATION_ROOT_FOLDER=${
                    debugApplicationRootFolder
                } is returned by getPathToExecutableDirectory(), make sure it is used for development only"
            )
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
