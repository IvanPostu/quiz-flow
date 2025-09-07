package com.iv127.quizflow.core.model.question.proc

import kotlin.test.assertNotNull
import org.junit.Test

class ProcessUtilsTest {

    @Test
    fun testGetExecutablePath() {
        val path = com.iv127.quizflow.core.platform.proc.PlatformProcess().getPathToExecutable()
        assertNotNull(path)
    }

    @Test
    fun testGetExecutableDirectoryPath() {
        val path = com.iv127.quizflow.core.platform.proc.PlatformProcess().getPathToExecutableDirectory()
        assertNotNull(path)
    }

}
