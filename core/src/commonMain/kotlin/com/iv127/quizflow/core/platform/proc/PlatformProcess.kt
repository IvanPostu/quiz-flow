package com.iv127.quizflow.core.platform.proc

expect class PlatformProcess {

    fun getPathToExecutable(): String

    fun getPathToExecutableDirectory(): String

    fun runShellScriptAndGetOutput(scriptContent: String): ProcessExecutionResult

}
