package com.iv127.quizflow.core.model.question

import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.PlatformProcess

class ResourceUtils(private val fileIo: FileIO, private val platformProcess: PlatformProcess) {
    fun readResource(path: String): ByteArray {
        return fileIo.readAll(platformProcess.getPathToExecutableDirectory() + "resources" + path)
    }
}
