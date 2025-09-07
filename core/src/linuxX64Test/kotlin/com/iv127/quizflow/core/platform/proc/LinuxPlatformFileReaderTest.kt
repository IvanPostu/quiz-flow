package com.iv127.quizflow.core.platform.proc


import com.iv127.quizflow.core.platform.file.PlatformFileReader
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.io.files.FileNotFoundException

class LinuxPlatformFileReaderTest {

    private lateinit var pathToFile: String

    @BeforeTest
    fun setup() {
        pathToFile = PlatformProcess().runShellScriptAndGetOutput(
            "" +
                "temp_file_path=$(mktemp);" +
                "echo -n 'Hello World' > \"\$temp_file_path\";" +
                "echo -n \$temp_file_path;"
        ).output
    }

    @Test
    fun testCloseUnopenedFile() {
        val reader = PlatformFileReader(pathToFile + "123")
        reader.close()
    }

    @Test
    fun testOpenFileThatDoesNotExist() {
        val reader = PlatformFileReader(pathToFile + "123")
        try {
            reader.open()
            fail("expect to throw")
        } catch (e: FileNotFoundException) {
            assertEquals("Can't open the file: ${pathToFile + "123"}, reason: No such file or directory", e.message)
        }
        reader.close()
    }

    @Test
    fun testReadFileContent() {
        val reader = PlatformFileReader(pathToFile)
        reader.open()

        val buffer = ByteArray(2)

        reader.setPosition(2)

        assertEquals(2, reader.readIntoBuffer(buffer))
        assertEquals("ll", buffer.decodeToString())

        assertEquals(2, reader.readIntoBuffer(buffer))
        assertEquals("o ", buffer.decodeToString())

        assertEquals(2, reader.readIntoBuffer(buffer))
        assertEquals("Wo", buffer.decodeToString())

        assertEquals(2, reader.readIntoBuffer(buffer))
        assertEquals("rl", buffer.decodeToString())

        assertEquals(1, reader.readIntoBuffer(buffer))
        assertEquals("dl", buffer.decodeToString())

        reader.setPosition(0)
        assertEquals(2, reader.readIntoBuffer(buffer))
        assertEquals("He", buffer.decodeToString())

        reader.close()
    }
}
