package com.iv127.quizflow.core.platform.proc

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class LinuxProcessUtilsTest {

    @Test
    fun testGetExecutablePath() {
        val path = PlatformProcess().getPathToExecutable()
        assertNotNull(path)
        assertNotEquals('/', path.last())
    }

    @Test
    fun testGetExecutableDirectoryPath() {
        val path = PlatformProcess().getPathToExecutableDirectory()
        assertNotNull(path)
        assertNotEquals('/', path.last())
    }

}
