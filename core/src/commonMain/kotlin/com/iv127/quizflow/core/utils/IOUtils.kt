package com.iv127.quizflow.core.utils

class IOUtils {
    companion object {
        fun byteArrayToString(byteArray: ByteArray): String {
            return byteArray.decodeToString()
        }

        fun mergeByteArrays(byteArrayList: List<ByteArray>): ByteArray {
            val size = byteArrayList.fold(0) { acc, byteArray ->
                acc + byteArray.size
            }
            val result = ByteArray(size)
            var offset = 0
            for (byteArray in byteArrayList) {
                arrayCopy(byteArray, 0, result, offset, byteArray.size)
                offset += byteArray.size
            }
            return result
        }

        fun arrayCopy(src: ByteArray, srcPos: Int, dest: ByteArray, destPos: Int, length: Int) {
            if (srcPos < 0 || destPos < 0 || length < 0 || srcPos + length > src.size || destPos + length > dest.size) {
                throw IndexOutOfBoundsException("Invalid index or length.")
            }

            for (i in 0 until length) {
                dest[destPos + i] = src[srcPos + i]
            }
        }
    }

}
