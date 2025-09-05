package com.iv127.quizflow.core.platform.file

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.io.files.FileNotFoundException
import platform.posix.SEEK_SET
import platform.posix._IO_FILE
import platform.posix.errno
import platform.posix.fclose
import platform.posix.fileno
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.fstat
import platform.posix.stat
import platform.posix.strerror

actual class PlatformFileReader actual constructor(filePath: String) {

    private val _filePath = filePath

    @OptIn(ExperimentalForeignApi::class)
    private var file: CPointer<_IO_FILE>? = null

    @OptIn(ExperimentalForeignApi::class)
    actual fun open() {
        file = fopen(_filePath, "rb")
        if (file == null) {
            val errorMessage = strerror(errno)?.toKString()
            throw FileNotFoundException("Can't open the file: $_filePath, reason: $errorMessage")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun setPosition(start: Long) {
        if (fseek(file!!, start, SEEK_SET) != 0) {
            throw IllegalStateException("Cannot set offset position for: $_filePath")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun getLength(): Long {
        val fd = fileno(file!!)

        val stat = nativeHeap.alloc<stat>()
        if (fstat(fd, stat.ptr) != 0) {
            throw IllegalStateException("Cannot get file length for: $_filePath")
        }
        return stat.st_size
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun readIntoBuffer(buffer: ByteArray): Int {
        val readBufferLength = buffer.size
        val bytesRead = fread(buffer.refTo(0), 1u, readBufferLength.toULong(), file!!).toInt()

        if (bytesRead == 0) {
            return -1
        }

        return bytesRead
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun close() {
        if (file != null) {
            fclose(file)
            file = null
        }
    }

}
