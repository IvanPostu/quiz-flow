package com.iv127.quizflow.core.platform.proc

import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import org.junit.Test

class JvmProcessUtilsTest {

    @Test
    fun testGetExecutablePath() {
        val path = com.iv127.quizflow.core.platform.proc.PlatformProcess().getPathToExecutable()
        assertNotNull(path)
        assertNotEquals('/', path.last())
    }

    @Test
    fun testGetExecutableDirectoryPath() {
        val path = com.iv127.quizflow.core.platform.proc.PlatformProcess().getPathToExecutableDirectory()
        assertNotNull(path)
        assertNotEquals('/', path.last())
    }

}
