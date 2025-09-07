package com.iv127.quizflow.core.platform.proc

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.util.Locale


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
        try {
            val os = System.getProperty("os.name").lowercase(Locale.getDefault())
            val processBuilder = if (os.contains("win")) {
                ProcessBuilder("cmd.exe", "/c", scriptContent)
            } else {
                ProcessBuilder("/bin/sh", "-c", scriptContent)
            }

            val process = processBuilder.start()


            val output = StringBuilder()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String
            while ((reader.readLine().also { line = it }) != null) {
                output.append(line).append(System.lineSeparator())
            }


            val errorOutput = StringBuilder()
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            var errorLine: String
            while ((errorReader.readLine().also { errorLine = it }) != null) {
                errorOutput.append(errorLine).append(System.lineSeparator())
            }


            val exitCode = process.waitFor()
            return ProcessExecutionResult(exitCode, output.toString(), errorOutput.toString())
        } catch (e: Exception) {
            throw IllegalStateException("Can't run: $scriptContent", e)
        }
    }
}
