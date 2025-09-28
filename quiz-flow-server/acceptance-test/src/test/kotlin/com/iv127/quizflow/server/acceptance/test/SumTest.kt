package com.iv127.quizflow.server.acceptance.test

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class SumTest {

    @Test
    fun testMul() {
        Assertions.assertEquals(6, 2 * 3)
    }

    @Test
    fun testSum() {
        Assertions.assertEquals(5, 2 + 3)
    }

}
