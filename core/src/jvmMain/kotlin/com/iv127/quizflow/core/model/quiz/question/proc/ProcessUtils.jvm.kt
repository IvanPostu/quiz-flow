package com.iv127.quizflow.core.model.quiz.question.proc

import java.io.File
import java.net.URL


actual class ProcessUtils {
    actual fun getPathToExecutable(): String {
        return internalGetPathToExecutable()
    }

    actual fun getPathToExecutableDirectory(): String {
        return internalGetPathToExecutable()
            .replaceAfterLast("/", "")
    }

    private fun internalGetPathToExecutable(): String {
        val location: URL = ProcessUtils::class.java.getProtectionDomain().codeSource.location
        val file = File(location.path)
        return file.absolutePath
    }
}
