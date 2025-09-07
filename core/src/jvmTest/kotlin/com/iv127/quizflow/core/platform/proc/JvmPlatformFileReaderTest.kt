package com.iv127.quizflow.core.platform.proc

import com.iv127.quizflow.core.platform.file.PlatformFileReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteExisting
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.io.files.FileNotFoundException
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat

class JvmPlatformFileReaderTest {

    private lateinit var tempFile: Path

    @BeforeTest
    @OptIn(ExperimentalTime::class)
    fun setup() {
        val tempFilePrefix = "" + Random.nextInt() + "_" + Clock.System.now().toEpochMilliseconds()
        tempFile = Files.createTempFile(tempFilePrefix, ".tmp")
        tempFile.deleteExisting()
    }

    @Test
    fun testCloseUnopenedFile() {
        val reader = PlatformFileReader(tempFile.absolutePathString() + "123")
        reader.close()
    }

    @Test
    fun testOpenFileThatDoesNotExist() {
        val reader = PlatformFileReader(tempFile.absolutePathString() + "123")
        Assertions.assertThatThrownBy({
            reader.open()
        }).isInstanceOf(FileNotFoundException::class.java)
        reader.close()
    }

    @Test
    fun testReadFileContent() {
        Files.write(tempFile, "Hello World".encodeToByteArray());

        val reader = PlatformFileReader(tempFile.absolutePathString())
        reader.open()

        val buffer = ByteArray(2)

        reader.setPosition(2)
        assertThat(reader.readIntoBuffer(buffer)).isEqualTo(2)
        assertThat(buffer.decodeToString()).isEqualTo("ll")
        assertThat(reader.readIntoBuffer(buffer)).isEqualTo(2)
        assertThat(buffer.decodeToString()).isEqualTo("o ")
        assertThat(reader.readIntoBuffer(buffer)).isEqualTo(2)
        assertThat(buffer.decodeToString()).isEqualTo("Wo")
        assertThat(reader.readIntoBuffer(buffer)).isEqualTo(2)
        assertThat(buffer.decodeToString()).isEqualTo("rl")
        assertThat(reader.readIntoBuffer(buffer)).isEqualTo(1)
        assertThat(buffer.decodeToString()).isEqualTo("dl")

        reader.setPosition(0)
        assertThat(reader.readIntoBuffer(buffer)).isEqualTo(2)
        assertThat(buffer.decodeToString()).isEqualTo("He")

        reader.close()
    }

}
