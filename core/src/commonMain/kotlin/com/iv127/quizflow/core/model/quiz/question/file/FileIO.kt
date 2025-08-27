package com.iv127.quizflow.core.model.quiz.question.file

expect class FileIO {

    fun readAll(filePath: String): ByteArray

    fun getPathSeparator(): String

}
