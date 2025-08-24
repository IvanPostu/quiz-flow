package com.iv127.quizflow.core.model.quiz.question.proc

import com.iv127.quizflow.core.model.quiz.question.proc.ProcessUtils
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessUtilsTest {

    @Test
    fun testGetExecutablePath() {
        val executablePath = ProcessUtils().getExecutablePath()
        assertTrue(executablePath.endsWith("/quiz-flow/core"))
    }

}
