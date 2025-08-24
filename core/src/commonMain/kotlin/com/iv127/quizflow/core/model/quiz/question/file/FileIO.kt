package com.iv127.quizflow.core.model.quiz.question.file

expect class FileIO {
    fun readAll(filePath: String): List<ByteArray>;
}
