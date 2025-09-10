package com.iv127.quizflow.core.sqlite

import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException


class JvmSqliteDatabase(dbPath: String) : SqliteDatabase {

    private val jdbcConnection: java.sql.Connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

    override fun executeAndGetResultSet(statement: String): List<Map<String, String>> {
        var attempts = 0
        while (true) {
            if (attempts > 0) {
                Thread.sleep(200); // 0.2 seconds
            }
            try {
                jdbcConnection.createStatement().use { stmt ->
                    return resultSetToList(stmt.executeQuery(statement))
                }
            } catch (e: SQLException) {
                // Hack, but there is no other way
                if (e.message == "[SQLITE_BUSY] The database file is locked (database is locked)" && attempts < 5) {
                    attempts++
                    continue
                }
                throw IllegalStateException("Database error: " + e.message, e)
            }
        }
    }


    override fun executeAndGetChangedRowsCount(statement: String): Int {
        var attempts = 0
        while (true) {
            if (attempts > 0) {
                Thread.sleep(200); // 0.2 seconds
            }
            try {
                jdbcConnection.createStatement().use { stmt ->
                    return stmt.executeUpdate(statement)
                }
            } catch (e: SQLException) {
                // Hack, but there is no other way
                if (e.message == "[SQLITE_BUSY] The database file is locked (database is locked)" && attempts < 5) {
                    attempts++
                    continue
                }
                throw IllegalStateException("Database error: " + e.message, e)
            }
        }
    }

    override fun close() {
        jdbcConnection.close()
    }

    @Throws(SQLException::class)
    private fun resultSetToList(rs: ResultSet): List<Map<String, String>> {
        val resultList: MutableList<Map<String, String>> = ArrayList()
        val columnCount = rs.metaData.columnCount
        while (rs.next()) {
            val rowMap: MutableMap<String, String> = HashMap()
            for (i in 1..columnCount) {
                val columnName = rs.metaData.getColumnName(i)
                val columnValue = rs.getString(i)
                rowMap[columnName] = columnValue
            }
            resultList.add(rowMap)
        }
        return resultList
    }

}
