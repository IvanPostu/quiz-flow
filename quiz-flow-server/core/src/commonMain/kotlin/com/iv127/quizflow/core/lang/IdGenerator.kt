package com.iv127.quizflow.core.lang

import kotlin.math.abs

class IdGenerator {
    companion object {
        fun getValue(): String {
            return "" + abs(xorShift(Any().hashCode()))
        }

        private fun xorShift(y: Int): Int {
            var x = y
            x = x xor (x shl 6)
            x = x xor (x ushr 21)
            x = x xor (x shl 7)
            return x
        }
    }
}
