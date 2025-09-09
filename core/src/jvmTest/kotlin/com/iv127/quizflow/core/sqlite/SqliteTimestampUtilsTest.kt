package com.iv127.quizflow.core.sqlite

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class SqliteTimestampUtilsTest {

    @OptIn(ExperimentalTime::class)
    @Test
    fun testSqliteTimestampUtils() {
        val initialDateTime = "2025-09-09 15:30:00"

        val instant = SqliteTimestampUtils.fromValue(initialDateTime)
        assertEquals(Instant.parse("2025-09-09T15:30:00Z"), instant)
        val sqliteValue = SqliteTimestampUtils.toValue(instant)
        assertEquals(initialDateTime, sqliteValue)
    }

}
