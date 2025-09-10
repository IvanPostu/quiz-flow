package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.platform.PlatformServices
import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.file.PlatformPath
import com.iv127.quizflow.core.platform.proc.PlatformProcess
import com.iv127.quizflow.core.resource.Resource
import com.iv127.quizflow.core.sqlite.LinuxSqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.migrator.DatabaseMigrator

internal class PlatformServicesImpl : PlatformServices {

    private val platformProcess: PlatformProcess = PlatformProcess()
    private val fileIo: FileIO = FileIO()
    private val resource: Resource = Resource(fileIo, platformProcess)
    private val platformPath = PlatformPath()

    override fun getProcessUtils(): PlatformProcess {
        return platformProcess
    }

    override fun getFileIO(): FileIO {
        return fileIo
    }

    override fun getPathUtils(): PlatformPath = platformPath

    override fun getResource(): Resource {
        return resource
    }

    override fun getSqliteDatabase(path: String, migrator: DatabaseMigrator?): SqliteDatabase {
        val db = LinuxSqliteDatabase(path)
        return migrator?.migrate(db) ?: db
    }

    override fun close() {
    }

}
