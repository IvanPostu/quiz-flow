package com.iv127.quizflow.core.sqlite

import java.sql.Date
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Time
import java.sql.Timestamp
import java.sql.Types


class JvmSqliteDatabase(private val dbPath: String) : SqliteDatabase {

    private val jdbcConnection: java.sql.Connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

    override fun executeAndGetResultSet(statement: String): List<Map<String, String>> {
        return executeWithRetriesAndGetResultSet(statement, emptyList())
    }

    override fun executeAndGetResultSet(statement: String, args: List<Any?>): List<Map<String, String?>> {
        return executeWithRetriesAndGetResultSet(statement, args)
    }

    override fun executeAndGetChangedRowsCount(statement: String): Int {
        return executeWithRetries(1000, 5) {
            jdbcConnection.createStatement().use { stmt ->
                stmt.executeUpdate(statement)
            }
        }
    }

    override fun executeAndGetChangedRowsCount(statement: String, args: List<Any?>): Int {
        return executeWithRetries(1000, 5) {
            jdbcConnection.prepareStatement(statement).use { stmt ->
                var i = 1
                for (arg in args) {
                    setPreparedStatementParameter(stmt, i++, arg)
                }
                stmt.executeUpdate()
            }
        }
    }

    override fun close() {
        jdbcConnection.close()
    }

    override fun getDatabasePath(): String {
        return dbPath
    }

    private fun executeWithRetriesAndGetResultSet(statement: String, args: List<Any?>): List<Map<String, String>> {
        return executeWithRetries(1000, 5) {
            jdbcConnection.prepareStatement(statement).use { stmt ->
                var i = 1
                for (arg in args) {
                    setPreparedStatementParameter(stmt, i++, arg)
                }
                resultSetToList(stmt.executeQuery())
            }
        }
    }

    private fun <T> executeWithRetries(timeRoomMillis: Long, retries: Int, closure: () -> T): T {
        val oneAttemptDuration = timeRoomMillis / retries
        var attempts = 0
        while (true) {
            if (attempts > 0) {
                Thread.sleep(oneAttemptDuration); // 0.2 seconds
            }
            try {
                return closure()
            } catch (e: SQLException) {
                // Hack, but there is no other way
                if (e.message == "[SQLITE_BUSY] The database file is locked (database is locked)" && attempts < retries) {
                    attempts++
                    continue
                }
                throw IllegalStateException("Database error: " + e.message, e)
            }
        }
    }

    @Throws(SQLException::class)
    private fun setPreparedStatementParameter(stmt: PreparedStatement, index: Int, param: Any?) {
        if (param == null) {
            stmt.setNull(index, Types.NULL)
        } else if (param is String) {
            stmt.setString(index, param)
        } else if (param is Int) {
            stmt.setInt(index, param)
        } else if (param is Double) {
            stmt.setDouble(index, param)
        } else if (param is Float) {
            stmt.setFloat(index, param)
        } else if (param is Long) {
            stmt.setLong(index, param)
        } else if (param is Boolean) {
            stmt.setBoolean(index, param)
        } else if (param is Date) {
            stmt.setDate(index, param as Date?)
        } else if (param is Time) {
            stmt.setTime(index, param)
        } else if (param is Timestamp) {
            stmt.setTimestamp(index, param as Timestamp?)
        } else {
            throw SQLException("Unsupported parameter type: " + param.javaClass.name)
        }
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
