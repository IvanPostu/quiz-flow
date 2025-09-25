package com.iv127.quizflow.core.lang

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class UUIDv4 {
    companion object {
        @OptIn(ExperimentalTime::class)
        fun generate(): String {
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val timeLow = (timestamp and 0xFFFFFFFFL).toInt()
            val timeMid = ((timestamp shr 32) and 0xFFFFL).toInt()
            val timeHighAndVersion = ((timestamp shr 48) and 0x0FFFL or 0x1000L).toInt() // Set version (1)

            val node = Random.nextLong(0, 0xFFFFFFFFFFFFL)

            return buildString {
                append(toHexStringPadded(timeLow, 8))       // 8 characters
                append('-')
                append(toHexStringPadded(timeMid, 4))       // 4 characters
                append('-')
                append(toHexStringPadded(timeHighAndVersion, 4)) // 4 characters (version is included here)
                append('-')
                append(toHexStringPadded(Random.nextInt(0, 0xFFFF), 4)) // 4 random characters
                append('-')
                append(toHexStringPadded(node.toInt(), 12))         // 12 random characters for node
            }
        }

        private fun toHexStringPadded(value: Int, length: Int): String {
            return value.toUInt().toString(16).padStart(length, '0')
        }
    }
}
