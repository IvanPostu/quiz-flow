package com.iv127.quizflow.core.model.question.file

import com.iv127.quizflow.core.utils.IOUtils
import kotlin.test.assertEquals
import org.junit.Test

class FileIOUtilsTest {

    @Test
    fun testFileIO() {
        val stringExample = "\uD83D\uDC36 Hello Ă"
        val byteArray = stringExample.toByteArray(Charsets.UTF_8)

        assertEquals(stringExample, IOUtils.byteArrayToString(byteArray))
    }

}
