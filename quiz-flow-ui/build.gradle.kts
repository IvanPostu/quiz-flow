tasks.register("runNpmBuild") {
    val currentProjectAbsolutePath: String = project(":quiz-flow-ui").file(".").absolutePath
    val commands: List<List<String>> = listOf(
        listOf("npm", "--prefix=$currentProjectAbsolutePath", "install"),
        listOf("npm", "--prefix=$currentProjectAbsolutePath", "run", "build"),
    )

    doLast {
        for (commandList in commands) {
            if (runProcessBlocking(commandList) != 0) {
                throw GradleException("Command failed with an exit code different from 0")
            }
        }
    }
}

private fun runProcessBlocking(commandList: List<String>): Int {
    val fullCommand = commandList.joinToString(separator = " ")
    try {
        val processBuilder = ProcessBuilder(commandList)
        processBuilder.redirectErrorStream(true)


        val process = processBuilder.start()
        process.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { println(it) }
            println()
        }

        val exitCode = process.waitFor()
        println("Command: ${fullCommand}, exit code: ${exitCode}")
        return exitCode
    } catch (e: Exception) {
        println("Error executing command: ${fullCommand}, message: ${e.message}")
        return 1
    }
}
