package com.iv127.quizflow.core.model.quiz.question.file

class FileIOUtils {

    companion object {
        fun byteListOfArraysToString(byteArrayList: List<ByteArray>): String {
            val expectedSize = byteArrayList.fold(0) { acc, byteArray ->
                acc + byteArray.size
            }

            val stringBuilder = StringBuilder(expectedSize)
            for (byteArray in byteArrayList) {
                stringBuilder.append(byteArray.decodeToString())
            }
            return stringBuilder.toString()
        }
    }

}
