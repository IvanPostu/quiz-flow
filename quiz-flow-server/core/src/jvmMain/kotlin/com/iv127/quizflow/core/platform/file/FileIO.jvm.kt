package com.iv127.quizflow.core.platform.file

import com.iv127.quizflow.core.utils.IOUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream


actual class FileIO {
    actual fun readAll(filePath: String): ByteArray {
        val bufferSize = 64 * 1024
        val result = mutableListOf<ByteArray>()

        try {
            BufferedInputStream(FileInputStream(filePath)).use { inputStream ->
                val buffer = ByteArray(bufferSize)
                var bytesRead: Int

                while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                    val batch = ByteArray(bytesRead)
                    System.arraycopy(buffer, 0, batch, 0, bytesRead)
                    result.add(batch)
                }
                return IOUtils.mergeByteArrays(result)
            }
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    actual fun getPathSeparator(): String {
        return File.separator
    }

}
