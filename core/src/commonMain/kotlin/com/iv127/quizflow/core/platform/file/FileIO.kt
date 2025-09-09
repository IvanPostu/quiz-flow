package com.iv127.quizflow.core.platform.file

// TODO should be replaced by PlatformFileReader
expect class FileIO {

    fun readAll(filePath: String): ByteArray

    fun getPathSeparator(): String

}
