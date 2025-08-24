package com.iv127.quizflow.core.model.quiz.question.file

import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException


actual class FileIO {
    actual fun readAll(filePath: String): List<ByteArray> {
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
                return result
            }
        } catch (_: IOException) {
        }
        return result
    }
}
