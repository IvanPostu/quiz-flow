package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.platform.PlatformServices
import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.PlatformProcess
import com.iv127.quizflow.core.sqlite.LinuxSqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteDatabase

internal class PlatformServicesImpl : PlatformServices {
    override fun getProcessUtils(): PlatformProcess {
        return PlatformProcess()
    }

    override fun getFileIO(): FileIO {
        return FileIO()
    }

    override fun close() {
    }

    override fun createSqliteDatabase(path: String): SqliteDatabase {
        return LinuxSqliteDatabase(path)
    }
}
