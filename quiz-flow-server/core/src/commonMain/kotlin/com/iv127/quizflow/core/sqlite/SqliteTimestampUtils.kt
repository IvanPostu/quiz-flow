package com.iv127.quizflow.core.sqlite

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class SqliteTimestampUtils {
    companion object {
        private val format = LocalDateTime.Format {
            year();
            char('-')
            monthNumber()
            char('-')
            day()
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
            char('.')
            secondFraction(fixedLength = 9)
        }

        @OptIn(ExperimentalTime::class)
        fun toValue(instant: Instant): String {
            val currentDateTime = instant.toLocalDateTime(TimeZone.UTC)
            return format.format(currentDateTime)
        }

        @OptIn(ExperimentalTime::class)
        fun fromValue(value: String): Instant {
            val dateTime = format.parse(value)
            return dateTime.toInstant(TimeZone.UTC)
        }
    }
}
