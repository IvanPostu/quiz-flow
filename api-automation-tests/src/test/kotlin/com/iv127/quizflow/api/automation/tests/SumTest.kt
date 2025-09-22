package com.iv127.quizflow.api.automation.tests

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class SumTest {

    @Test
    fun testMul() {
        Assertions.assertEquals(6, Sum().mul(2, 3))
    }

    @Test
    fun testSum() {
        Assertions.assertEquals(5, Sum().sum(2, 31))
    }

}
