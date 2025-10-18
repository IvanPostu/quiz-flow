package com.iv127.quizflow.core.lang

import kotlin.test.Test
import kotlin.test.assertEquals

class Sha256Test {

    @Test
    fun testSha256() {
        assertEquals("9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", sha256("test"))
        assertEquals("25581b4a809d3824d1b822853f8c0e8f7f7a366c5a0ceb2d3c4a625d82b2c67b", sha256("123aba"))
        assertEquals("872e4e50ce9990d8b041330c47c9ddd11bec6b503ae9386a99da8584e9bb12c4", sha256("HelloWorld"))
    }

    private fun sha256(input: String): String {
        return Sha256.hashToHex(input.encodeToByteArray())
    }

}
