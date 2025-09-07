package com.iv127.quizflow.core.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKStringFromUtf8
import libsimple.concat

class SimpleLibTest {

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun testConcat() {
        memScoped {
            val str1 = "Test".cstr.getPointer(this)
            val str2 = "Example".cstr.getPointer(this)

            val str3 = concat(str1, str2)
            val kotlinString = str3?.toKStringFromUtf8() ?: throw IllegalStateException()

            assertEquals("TestExample", kotlinString)
        }
    }

}
