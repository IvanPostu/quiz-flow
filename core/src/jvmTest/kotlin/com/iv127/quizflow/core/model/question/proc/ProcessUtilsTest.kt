package com.iv127.quizflow.core.model.question.proc

import com.iv127.quizflow.core.platform.proc.ProcessUtils
import kotlin.test.assertNotNull
import org.junit.Test

class ProcessUtilsTest {

    @Test
    fun testGetExecutablePath() {
        val path = com.iv127.quizflow.core.platform.proc.ProcessUtils().getPathToExecutable()
        assertNotNull(path)
    }

    @Test
    fun testGetExecutableDirectoryPath() {
        val path = com.iv127.quizflow.core.platform.proc.ProcessUtils().getPathToExecutableDirectory()
        assertNotNull(path)
    }

}
