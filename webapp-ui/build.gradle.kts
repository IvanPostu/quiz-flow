plugins {
    id("java")
    kotlin("jvm")
}

tasks.register("runNpmBuild") {
    doLast {
        val currentProjectAbsolutePath = project(":webapp-ui").file(".").absolutePath

        try {
            val processBuilder = ProcessBuilder("npm", "--prefix=$currentProjectAbsolutePath", "run", "build")
            processBuilder.redirectErrorStream(true)


            val process = processBuilder.start()
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { println(it) }
                println()
            }

            val exitCode = process.waitFor()
            println("Process exited with code: $exitCode")

        } catch (e: Exception) {
            println("Error executing command: ${e.message}")
        }
    }
}
