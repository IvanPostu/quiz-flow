package com.iv127.quizflow.core.sqlite

class LinuxSqliteDatabase(dbPath: String) : SqliteDatabase {

    private val kSqlite: KSqlite = KSqlite(dbPath)

    override fun execute(statement: String): List<Map<String, String>> {
        val result: MutableList<Map<String, String>> = ArrayList()
        kSqlite.execute(statement) { cols, data ->
            val map: LinkedHashMap<String, String> = LinkedHashMap()
            for (i in cols.indices) {
                val col = cols[i]
                val value = data[i]
                map.put(col, value)
            }
            result.add(map)
            0
        }
        throw Exception()
    }

    override fun close() {
        kSqlite.close()
    }

}
