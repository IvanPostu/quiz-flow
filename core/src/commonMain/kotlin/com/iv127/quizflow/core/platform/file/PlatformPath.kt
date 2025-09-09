package com.iv127.quizflow.core.platform.file

expect class PlatformPath {

    fun getFilenamesFromDirectory(directoryPath: String): List<String>

    fun getPathSeparator(): String

    fun resolve(first: String, vararg others: String): String

}
