package com.iv127.quizflow.core.platform

import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.PlatformProcess
import com.iv127.quizflow.core.sqlite.SqliteDatabase

interface PlatformServices : AutoCloseable {

    fun getProcessUtils(): PlatformProcess

    fun getFileIO(): FileIO

    fun createSqliteDatabase(path: String): SqliteDatabase

}
