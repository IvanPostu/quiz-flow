package com.iv127.quizflow.core.platform.file

import com.iv127.quizflow.core.platform.proc.PlatformProcess
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class JvmPlatformPathTest {


    private lateinit var pathToFile: String

    @BeforeTest
    fun setup() {
        pathToFile = PlatformProcess().runShellScriptAndGetOutput(
            """
                temp_dir=${'$'}(mktemp -d) \
                  && echo 'a' > "${'$'}temp_dir/a.txt" \
                  && echo 'b' > "${'$'}temp_dir/b.txt" && echo 'c' > "${'$'}temp_dir/c.txt" \
                  && echo -n ${'$'}temp_dir
            """.trimIndent()
        ).output
    }

    @Test
    fun testFetFilenamesFromDirectory() {
        val path = PlatformPath()
        val files = path.getFilenamesFromDirectory(pathToFile)
        assertEquals(3, files.size)

        val sortedFiles = files.sorted()
        assertEquals("a.txt", sortedFiles[0])
        assertEquals("b.txt", sortedFiles[1])
        assertEquals("c.txt", sortedFiles[2])
    }

}
