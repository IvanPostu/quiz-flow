package com.iv127.quizflow.core.platform.file

import java.io.File

actual class PathUtils {
    actual fun getFilenamesFromDirectory(directoryPath: String): List<String> {
        val directory = File(directoryPath)
        return if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.map { it.name } ?: emptyList()
        } else {
            emptyList()
        }
    }
}
