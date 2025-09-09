package com.iv127.quizflow.core.platform.file

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.closedir
import platform.posix.errno
import platform.posix.opendir
import platform.posix.readdir
import platform.posix.strerror

actual class PlatformPath {

    @OptIn(ExperimentalForeignApi::class)
    actual fun getFilenamesFromDirectory(directoryPath: String): List<String> {
        val filenames = mutableListOf<String>()

        val dir = opendir(directoryPath)

        if (dir == null) {
            println("Failed to open directory: $directoryPath")
            println("Error: ${strerror(errno)?.toKString()}")
            throw IllegalStateException("Failed to open directory: $directoryPath")
        }

        var entry = readdir(dir)

        while (entry != null) {
            val name = entry.pointed.d_name.toKString()
            if (name != "." && name != "..") {
                filenames.add(name)
            }
            entry = readdir(dir)
        }

        closedir(dir)
        return filenames
    }

    actual fun getPathSeparator(): String {
        return "/"
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
