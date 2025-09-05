package com.iv127.quizflow.core.platform.file

import java.io.RandomAccessFile

actual class PlatformFileReader actual constructor(filePath: String) {
    private val file = RandomAccessFile(filePath, "r")

    actual fun setPosition(start: Long) {
        file.seek(start)
    }

    actual fun getLength(): Long {
        return file.length()
    }

    actual fun readIntoBuffer(buffer: ByteArray): Int {
        return file.read(buffer)
    }

    actual fun close() {
        file.close()
    }

}
