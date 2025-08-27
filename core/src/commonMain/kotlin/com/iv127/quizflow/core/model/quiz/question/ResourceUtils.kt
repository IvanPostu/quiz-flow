package com.iv127.quizflow.core.model.quiz.question

import com.iv127.quizflow.core.model.quiz.question.file.FileIO
import com.iv127.quizflow.core.model.quiz.question.proc.ProcessUtils

class ResourceUtils(private val fileIo: FileIO, private val processUtils: ProcessUtils) {
    fun readResource(path: String): ByteArray {
        return fileIo.readAll(processUtils.getPathToExecutableDirectory() + "resources" + path)
    }
}
