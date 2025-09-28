package com.iv127.quizflow.core.sqlite

interface SqliteDatabase : AutoCloseable {

    fun executeAndGetResultSet(statement: String): List<Map<String, String?>>

    fun executeAndGetResultSet(statement: String, args: List<Any?>): List<Map<String, String?>>

    fun executeAndGetChangedRowsCount(statement: String): Int

    fun executeAndGetChangedRowsCount(statement: String, args: List<Any?>): Int

    fun getDatabasePath(): String

}
