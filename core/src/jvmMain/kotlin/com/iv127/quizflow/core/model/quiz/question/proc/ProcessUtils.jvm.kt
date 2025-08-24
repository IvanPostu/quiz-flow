package com.iv127.quizflow.core.model.quiz.question.proc

import java.nio.file.Paths

actual class ProcessUtils {
    actual fun getExecutablePath(): String {
        return Paths.get("").toAbsolutePath().toString()
    }
}
