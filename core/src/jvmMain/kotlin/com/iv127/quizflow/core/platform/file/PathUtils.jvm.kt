package com.iv127.quizflow.core.platform.file

import java.io.File

actual class PlatformPath {
    actual fun getFilenamesFromDirectory(directoryPath: String): List<String> {
        val directory = File(directoryPath)
        return if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.map { it.name } ?: emptyList()
        } else {
            emptyList()
        }
    }

    actual fun getPathSeparator(): String {
        return File.separator
    }

    actual fun resolve(first: String, vararg others: String): String {
        val resultBuilder = StringBuilder(first)
        for (part in others) {
            resultBuilder
                .append(getPathSeparator())
                .append(part)
        }
        return resultBuilder.toString()
    }
}
