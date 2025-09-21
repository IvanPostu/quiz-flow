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
    fun testUpdateAndGetChangedRowsCount() {
        LinuxSqliteDatabase(pathToFile).use { sqliteDatabase ->
            try {
                sqliteDatabase.executeAndGetResultSet(
                    """
                    CREATE TABLE users (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL
                    );
                """.trimIndent()
                )
                sqliteDatabase.executeAndGetResultSet(
                    """
                    INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com');
                    INSERT INTO users (name, email) VALUES ('Bob', 'bob@example.com');
                    INSERT INTO users (name, email) VALUES ('Charlie', 'charlie@example.com');
                    SELECT last_insert_rowid() AS lastId;
                """.trimIndent()
                )

                val changedRowsCount = sqliteDatabase.executeAndGetChangedRowsCount(
                    """
                    UPDATE users SET email='changed@mail.com' WHERE users.name IN ('Alice', 'Bob');
                """.trimIndent()
                )
                assertEquals(2, changedRowsCount)
            } finally {
                sqliteDatabase.close()
            }
        }
    }

    @Test
    fun testSelect() {
        LinuxSqliteDatabase(pathToFile).use { sqliteDatabase ->
            val result = sqliteDatabase.executeAndGetResultSet("SELECT 1 AS a, 2 AS b;")

            assertEquals(1, result.size)
            val row = result.first()

            assertEquals(2, row.size)
            assertEquals("1", row["a"])
            assertEquals("2", row["b"])
        }

    }

    @Test
    fun testPreparedSelect() {
        LinuxSqliteDatabase(pathToFile).use { sqlite1 ->
            sqlite1.executeAndGetChangedRowsCount(
                """
                        CREATE TABLE users (
                            id INTEGER PRIMARY KEY,
                            name TEXT NOT NULL,
                            email TEXT NOT NULL
                        );
                   """.trimIndent()
            )

            sqlite1.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            sqlite1.executeAndGetChangedRowsCount(
                """
                        INSERT INTO users (name, email) VALUES ('N1', 'a1@example.com');
                        INSERT INTO users (name, email) VALUES ('N2', 'a2@example.com');
                        INSERT INTO users (name, email) VALUES ('N3', 'a3@example.com');
                    """.trimIndent()
            )
            sqlite1.executeAndGetChangedRowsCount("COMMIT;")

            val result = sqlite1.executeAndGetResultSet("SELECT * FROM users WHERE name=?", listOf("N2"))
            assertEquals(1, result.size)
            assertEquals("2", result[0]["id"])
            assertEquals("N2", result[0]["name"])
            assertEquals("a2@example.com", result[0]["email"])
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testTransaction() {
        println(pathToFile)
        LinuxSqliteDatabase(pathToFile).use { sqlite1 ->
            LinuxSqliteDatabase(pathToFile).use { sqlite2 ->
                sqlite1.executeAndGetResultSet(
                    """
                        CREATE TABLE users (
                            id INTEGER PRIMARY KEY,
                            name TEXT NOT NULL,
                            email TEXT NOT NULL
                        );
                   """.trimIndent()
                )

                sqlite1.executeAndGetResultSet("BEGIN TRANSACTION;")
                sqlite1.executeAndGetResultSet(
                    """
                        INSERT INTO users (name, email) VALUES ('N1', 'a@example.com');
                        INSERT INTO users (name, email) VALUES ('N2', 'a@example.com');
                        INSERT INTO users (name, email) VALUES ('N3', 'a@example.com');
                    """.trimIndent()
                )
                sqlite1.executeAndGetResultSet("COMMIT;")

                sqlite1.executeAndGetResultSet("BEGIN TRANSACTION;")
                sqlite1.executeAndGetResultSet(
                    """
                        INSERT INTO users (name, email) VALUES ('N4', 'a@example.com');
                    """.trimIndent()
                )

                sqlite2.executeAndGetResultSet("BEGIN TRANSACTION;")
                sqlite2.executeAndGetResultSet("SELECT * FROM users")
                val now = Clock.System.now().toEpochMilliseconds()
                try {
                    sqlite2.executeAndGetResultSet(
                        """
                            INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com');
                        """.trimIndent()
                    )
                    fail()
                } catch (e: IllegalStateException) {
                    val timeTaken = Clock.System.now().toEpochMilliseconds() - now
                    assertTrue(timeTaken >= 800)
                    assertEquals(e.message, "sqlite3_exec failed with code: 5 - database is locked")
                }
            }
        }

    }

}
