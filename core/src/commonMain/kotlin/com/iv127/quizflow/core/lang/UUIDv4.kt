package com.iv127.quizflow.core.lang

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class UUIDv4 {
    companion object {
        @OptIn(ExperimentalTime::class)
        fun generate(format: Format = Format.DASHED): String {
            val timestamp: Long = Clock.System.now().toEpochMilliseconds()
            val timeLow = (timestamp and 0xFFFFFFFFL).toInt()
            val timeMid = ((timestamp shr 32) and 0xFFFFL).toInt()
            val timeHighAndVersion = ((timestamp shr 48) and 0x0FFFL or 0x1000L).toInt() // Set version (1)

            val node = Random.nextLong(0, 0xFFFFFFFFFFFFL)
            val separatorCharacter = if (format == Format.DASHED) '-' else ' '

            return buildString {
                append(toHexStringPadded(timeLow, 8))       // 8 characters
                append(separatorCharacter)
                append(toHexStringPadded(timeMid, 4))       // 4 characters
                append(separatorCharacter)
                append(toHexStringPadded(timeHighAndVersion, 4)) // 4 characters (version is included here)
                append(separatorCharacter)
                append(toHexStringPadded(Random.nextInt(0, 0xFFFF), 4)) // 4 random characters
                append(separatorCharacter)
                append(toHexStringPadded(node.toInt(), 12))         // 12 random characters for node
            }
        }

        private fun toHexStringPadded(value: Int, length: Int): String {
            return value.toUInt().toString(16).padStart(length, '0')
        }

        enum class Format {
            DASHED,
            COMPACT,
        }
    }
}
