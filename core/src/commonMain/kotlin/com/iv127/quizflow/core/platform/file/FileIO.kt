package com.iv127.quizflow.core.platform.file

expect class FileIO {

    fun readAll(filePath: String): ByteArray

    fun getPathSeparator(): String

}
