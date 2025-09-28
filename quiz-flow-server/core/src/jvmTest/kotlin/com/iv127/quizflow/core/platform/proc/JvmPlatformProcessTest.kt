package com.iv127.quizflow.core.platform.proc

import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import org.junit.Test

class JvmPlatformProcessTest {

    @Test
    fun testGetExecutableDirectoryPath() {
        val path = PlatformProcess().getPathToExecutableDirectory()
        assertNotNull(path)
        assertNotEquals('/', path.last())
    }

}
