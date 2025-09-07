package com.iv127.quizflow.core.sqlite

interface SqliteDatabase : AutoCloseable {

    fun execute(statement: String): List<Map<String, String>>

}
