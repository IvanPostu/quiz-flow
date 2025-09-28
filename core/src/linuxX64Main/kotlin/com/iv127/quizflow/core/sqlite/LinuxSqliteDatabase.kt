package com.iv127.quizflow.core.sqlite

class LinuxSqliteDatabase(private val dbPath: String) : SqliteDatabase {

    private val kSqlite: KSqlite = KSqlite(dbPath)

    override fun executeAndGetResultSet(statement: String): List<Map<String, String?>> {
        val result: MutableList<Map<String, String?>> = ArrayList()
        kSqlite.execute(statement) { cols, data ->
            val map: LinkedHashMap<String, String?> = LinkedHashMap()
            for (i in cols.indices) {
                val col = cols[i]
                val value = data[i]
                map[col!!] = value
            }
            result.add(map)
            0
        }
        return result
    }


    override fun executeAndGetResultSet(statement: String, args: List<Any?>): List<Map<String, String?>> {
        val result: MutableList<Map<String, String?>> = ArrayList()
        kSqlite.executeStatement(statement, args) { cols, data ->
            val map: LinkedHashMap<String, String?> = LinkedHashMap()
            for (i in cols.indices) {
                val col = cols[i]
                val value = data[i]
                map[col!!] = value
            }
            result.add(map)
            0
        }
        return result
    }


    override fun executeAndGetChangedRowsCount(statement: String): Int {
        return kSqlite.execute(statement)
    }

    override fun executeAndGetChangedRowsCount(statement: String, args: List<Any?>): Int {
        return kSqlite.executeStatement(statement, args)
    }

    override fun getDatabasePath(): String {
        return dbPath
    }

    override fun close() {
        kSqlite.close()
    }

}
