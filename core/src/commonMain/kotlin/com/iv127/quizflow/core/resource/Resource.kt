package com.iv127.quizflow.core.resource

import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.PlatformProcess

class Resource(private val fileIo: FileIO, private val platformProcess: PlatformProcess) {

    private val _resourcePath = StringBuilder(platformProcess.getPathToExecutableDirectory())
        .append(fileIo.getPathSeparator())
        .append("resources")
        .toString()

    val resourcePath: String get() = _resourcePath

    fun readResource(first: String, vararg more: String): ByteArray {
        val pathBuilder = StringBuilder(resourcePath)
        pathBuilder.append(fileIo.getPathSeparator())
        pathBuilder.append(first)

        for (item in more) {
            pathBuilder.append(fileIo.getPathSeparator())
            pathBuilder.append(item)
        }

        return fileIo.readAll(pathBuilder.toString())
    }

}
