package com.iv127.quizflow.core.sqlite

interface SqliteDatabase : AutoCloseable {

    fun executeAndGetResultSet(statement: String): List<Map<String, String?>>

    fun executeAndGetChangedRowsCount(statement: String): Int

}
