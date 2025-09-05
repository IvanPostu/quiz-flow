package com.iv127.quizflow.core.platform.file

import com.iv127.quizflow.core.utils.IOUtils
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readBytes
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread

actual class FileIO {
    actual fun readAll(filePath: String): ByteArray {
        return IOUtils.mergeByteArrays(internalReadAll(filePath))
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun internalReadAll(filePath: String): List<ByteArray> {
        val file = fopen(filePath, "rb") ?: throw IllegalArgumentException("Cannot open input file $filePath")
        val result = mutableListOf<ByteArray>()
        try {
            memScoped {
                val readBufferLength = 64 * 1024
                val buffer = allocArray<ByteVar>(readBufferLength)

                var bytesRead: Int
                while (true) {
                    bytesRead = fread(buffer, 1u, readBufferLength.toULong(), file).toInt()
                    if (bytesRead == 0) break
                    result.add(cArrayPointerToByteArray(buffer, bytesRead))
                }
            }
            return result
        } catch (e: Exception) {
            throw IllegalStateException(e)
        } finally {
            fclose(file)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun cArrayPointerToByteArray(cArrayPointer: CArrayPointer<ByteVar>, size: Int): ByteArray {
        val byteArray = cArrayPointer.readBytes(size)
        return byteArray
    }

    actual fun getPathSeparator(): String {
        return "/"
    }

}
