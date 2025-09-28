package com.iv127.quizflow.core.platform.file

import com.iv127.quizflow.core.platform.proc.PlatformProcess
import kotlin.test.Test
import kotlin.test.assertEquals

class LinuxPlatformProcessTest {

    @Test
    fun testRunValidCommand() {
        val result = PlatformProcess().runShellScriptAndGetOutput(
            "echo -n 'aa'"
        )
        assertEquals(0, result.returnCode)
        assertEquals("aa", result.output)
    }

    @Test
    fun testRunInvalidCommand() {
        val result = PlatformProcess().runShellScriptAndGetOutput(
            "cat aa"
        )
        assertEquals(256, result.returnCode)
        assertEquals("cat: aa: No such file or directory\n", result.output)
    }

}
