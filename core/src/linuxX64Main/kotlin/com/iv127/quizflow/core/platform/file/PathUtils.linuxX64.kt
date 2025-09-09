package com.iv127.quizflow.core.platform.file

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import platform.posix.closedir
import platform.posix.errno
import platform.posix.opendir
import platform.posix.readdir
import platform.posix.strerror

actual class PathUtils {

    @OptIn(ExperimentalForeignApi::class)
    actual fun getFilenamesFromDirectory(directoryPath: String): List<String> {
        val filenames = mutableListOf<String>()

        // Attempt to open the directory
        val dir = opendir(directoryPath)

        if (dir == null) {
            // Print the error message if opendir fails
            println("Failed to open directory: $directoryPath")
            println("Error: ${strerror(errno)?.toKString()}")
            throw IllegalStateException("Failed to open directory: $directoryPath")
        }

        // Read directory contents if opened successfully
        var entry = readdir(dir)

        while (entry != null) {
            val name = entry.pointed.d_name.toKString()
            if (name != "." && name != "..") {
                filenames.add(name)
            }
            entry = readdir(dir)
        }

        // Close the directory
        closedir(dir)

        return filenames
    }

}
