package com.iv127.quizflow.core.model.quiz.question.proc

import kotlin.test.assertNotNull
import org.junit.Test

class ProcessUtilsTest {

    @Test
    fun testGetExecutablePath() {
        val path = ProcessUtils().getPathToExecutable()
        assertNotNull(path)
    }

    @Test
    fun testGetExecutableDirectoryPath() {
        val path = ProcessUtils().getPathToExecutableDirectory()
        assertNotNull(path)
    }

}
