package com.iv127.quizflow.core.platform.sqlite

import com.iv127.quizflow.core.platform.proc.PlatformProcess
import com.iv127.quizflow.core.sqlite.LinuxSqliteDatabase
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class LinuxSqliteDatabaseTest {
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
    fun testSelect() {
        LinuxSqliteDatabase(pathToFile).use { sqliteDatabase ->
            val result = sqliteDatabase.execute("SELECT 1 AS a, 2 AS b;")

            assertEquals(1, result.size)
            val row = result.first()

            assertEquals(2, row.size)
            assertEquals("1", row["a"])
            assertEquals("2", row["b"])
        }

    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testTransaction() {
        println(pathToFile)
        LinuxSqliteDatabase(pathToFile).use { sqlite1 ->
            LinuxSqliteDatabase(pathToFile).use { sqlite2 ->
                sqlite1.execute(
                    """
                        CREATE TABLE users (
                            id INTEGER PRIMARY KEY,
                            name TEXT NOT NULL,
                            email TEXT NOT NULL
                        );
                   """.trimIndent()
                )

                sqlite1.execute("BEGIN TRANSACTION;")
                sqlite1.execute(
                    """
                        INSERT INTO users (name, email) VALUES ('N1', 'a@example.com');
                        INSERT INTO users (name, email) VALUES ('N2', 'a@example.com');
                        INSERT INTO users (name, email) VALUES ('N3', 'a@example.com');
                    """.trimIndent()
                )
                sqlite1.execute("COMMIT;")

                sqlite1.execute("BEGIN TRANSACTION;")
                sqlite1.execute(
                    """
                        INSERT INTO users (name, email) VALUES ('N4', 'a@example.com');
                    """.trimIndent()
                )

                sqlite2.execute("BEGIN TRANSACTION;")
                sqlite2.execute("SELECT * FROM users")
                val now = Clock.System.now().toEpochMilliseconds()
                try {
                    sqlite2.execute(
                        """
                            INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com');
                        """.trimIndent()
                    )
                    fail()
                } catch (e: IllegalStateException) {
                    assertTrue(Clock.System.now().toEpochMilliseconds() - now >= 800)
                    assertEquals(e.message, "sqlite3_exec failed with code: 5 - database is locked")
                }
            }
        }

    }

}
