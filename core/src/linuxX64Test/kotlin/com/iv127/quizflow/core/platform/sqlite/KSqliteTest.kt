package com.iv127.quizflow.core.platform.sqlite

import com.iv127.quizflow.core.platform.proc.PlatformProcess
import com.iv127.quizflow.core.sqlite.KSqlite
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KSqliteTest {

    private lateinit var pathToFile: String

    @BeforeTest
    fun setup() {
        pathToFile = PlatformProcess().runShellScriptAndGetOutput(
            "" +
                "temp_file_path=$(mktemp --suffix='.db');" +
                "echo -n \$temp_file_path;"
        ).output
    }

    @Test
    fun testCreateTableInsertAndSelectDataFromSqliteDatabase() {
        val ksqlite = KSqlite(pathToFile)
        try {
            var columns: Array<String> = arrayOf()
            var dataRecords: Array<String> = arrayOf()
            var count = 0
            ksqlite.execute(
                """
                    CREATE TABLE users (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL
                    );
                """.trimIndent()
            ) { cols, data ->
                count++
                0
            }
            assertEquals(0, count, "Assert callback isn't executed")

            ksqlite.execute(
                """
                    INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com');
                    INSERT INTO users (name, email) VALUES ('Bob', 'bob@example.com');
                    INSERT INTO users (name, email) VALUES ('Charlie', 'charlie@example.com');
                """.trimIndent()
            ) { cols, data ->
                count++
                0
            }
            assertEquals(0, count, "Assert callback isn't executed")

            ksqlite.execute(
                """
                    SELECT * FROM users;
                    SELECT 1 AS e;
                """.trimIndent()
            ) { cols, data ->
                columns = cols
                dataRecords = data
                count++
                0
            }
            assertEquals(4, count, "Assert callback is executed 4 times")
        } finally {
            ksqlite.close()
        }
    }

}
