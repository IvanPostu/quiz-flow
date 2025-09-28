package com.iv127.quizflow.core.platform

import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.file.PlatformPath
import com.iv127.quizflow.core.platform.proc.PlatformProcess
import com.iv127.quizflow.core.resource.Resource
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.migrator.DatabaseMigrator

interface PlatformServices : AutoCloseable {

    fun getProcessUtils(): PlatformProcess

    fun getFileIO(): FileIO

    fun getPathUtils(): PlatformPath

    fun getResource(): Resource;

    fun getSqliteDatabase(path: String, migrator: DatabaseMigrator? = null): SqliteDatabase

}
