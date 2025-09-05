package com.iv127.quizflow.core.platform.file

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import platform.posix.SEEK_SET
import platform.posix.fclose
import platform.posix.fileno
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.fstat
import platform.posix.memcpy
import platform.posix.stat

actual class PlatformFileReader actual constructor(filePath: String) {

    @OptIn(ExperimentalForeignApi::class)
    private val file = fopen(filePath, "rb") ?: throw IllegalArgumentException("Cannot open input file $filePath")
    private val _filePath = filePath

    @OptIn(ExperimentalForeignApi::class)
    actual fun setPosition(start: Long) {
        if (fseek(file, start, SEEK_SET) != 0) {
            throw IllegalStateException("Cannot set offset position for: $_filePath")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun getLength(): Long {
        val fd = fileno(file)

        val stat = nativeHeap.alloc<stat>()
        if (fstat(fd, stat.ptr) != 0) {
            throw IllegalStateException("Cannot get file length for: $_filePath")
        }
        return stat.st_size
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun readIntoBuffer(buffer: ByteArray): Int {
        memScoped {
            val readBufferLength = buffer.size
            val cBuffer = allocArray<ByteVar>(readBufferLength)
            val bytesRead = fread(cBuffer, 1u, readBufferLength.toULong(), file).toInt()

            if (bytesRead == 0) {
                return -1
            }

            memcpy(buffer.refTo(0), cBuffer, buffer.size.toULong())
            return bytesRead
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun close() {
        fclose(file)
    }

}
