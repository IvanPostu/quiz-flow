package com.iv127.quizflow.core.sqlite

import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteExisting
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


class JvmSqliteDatabaseTest {
    private lateinit var pathToFile: String

    @BeforeTest
    @OptIn(ExperimentalTime::class)
    fun setup() {
        val tempFilePrefix = "" + Random.nextInt() + "_" + Clock.System.now().toEpochMilliseconds()
        val tempFile = Files.createTempFile(tempFilePrefix, ".db")
        tempFile.deleteExisting()
        pathToFile = tempFile.absolutePathString()
    }


    @Test
    fun testUpdateAndGetChangedRowsCount() {
        JvmSqliteDatabase(pathToFile).use { sqliteDatabase ->
            var changedRowsCount = 0

            changedRowsCount = sqliteDatabase.executeAndGetChangedRowsCount(
                """
                    CREATE TABLE users (
                        id INTEGER PRIMARY KEY,
                        name TEXT NOT NULL,
                        email TEXT NOT NULL
                    );
                """.trimIndent()
            )
            assertEquals(0, changedRowsCount)

            changedRowsCount = sqliteDatabase.executeAndGetChangedRowsCount(
                """
                    INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com');
                    INSERT INTO users (name, email) VALUES ('Bob', 'bob@example.com');
                    INSERT INTO users (name, email) VALUES ('Charlie', 'charlie@example.com');
                """.trimIndent()
            )
            assertEquals(3, changedRowsCount)
            changedRowsCount = sqliteDatabase.executeAndGetChangedRowsCount(
                """
                    INSERT INTO users (name, email) VALUES ('Charlie', 'charlie@example.com');
                """.trimIndent()
            )
            assertEquals(1, changedRowsCount)

            val lastId = sqliteDatabase.executeAndGetResultSet("SELECT last_insert_rowid() AS lastId;")[0]["lastId"]
                ?.toInt()
            assertEquals(4, lastId)

            changedRowsCount = sqliteDatabase.executeAndGetChangedRowsCount(
                """
                    UPDATE users SET email='changed@mail.com' WHERE users.name IN ('Alice', 'Bob');
                """.trimIndent()
            )
            assertEquals(2, changedRowsCount)

        }
    }


    @Test
    fun testSelect() {
        JvmSqliteDatabase(pathToFile).use { sqliteDatabase ->
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
        JvmSqliteDatabase(pathToFile).use { sqlite1 ->
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
        JvmSqliteDatabase(pathToFile).use { sqlite1 ->

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
                        INSERT INTO users (name, email) VALUES ('N1', 'a@example.com');
                        INSERT INTO users (name, email) VALUES ('N2', 'a@example.com');
                        INSERT INTO users (name, email) VALUES ('N3', 'a@example.com');
                    """.trimIndent()
            )
            sqlite1.executeAndGetChangedRowsCount("COMMIT;")

            sqlite1.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            sqlite1.executeAndGetChangedRowsCount(
                """
                        INSERT INTO users (name, email) VALUES ('N4', 'a@example.com');
                    """.trimIndent()
            )

            JvmSqliteDatabase(pathToFile).use { sqlite2 ->
                sqlite2.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
                sqlite2.executeAndGetResultSet("SELECT * FROM users")
                val now = Clock.System.now().toEpochMilliseconds()
                try {
                    sqlite2.executeAndGetChangedRowsCount(
                        """
                            INSERT INTO users (name, email) VALUES ('Alice', 'alice@example.com');
                        """.trimIndent()
                    )
                    fail()
                } catch (e: IllegalStateException) {
                    val timeTaken = Clock.System.now().toEpochMilliseconds() - now
                    assertTrue(timeTaken in 1000..1100, "Expected $timeTaken to be between 1000 and 1100")
                    assertEquals(
                        e.message,
                        "Database error: [SQLITE_BUSY] The database file is locked (database is locked)"
                    )
                }
            }
            JvmSqliteDatabase(pathToFile).use { sqlite2 ->
                sqlite2.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
                sqlite2.executeAndGetResultSet("SELECT * FROM users")
                val now = Clock.System.now().toEpochMilliseconds()
                try {
                    sqlite2.executeAndGetChangedRowsCount(
                        """
                            INSERT INTO users (name, email) VALUES (?, 'alice@example.com');
                        """.trimIndent(),
                        listOf("Aaaaa")
                    )
                    fail()
                } catch (e: IllegalStateException) {
                    val timeTaken = Clock.System.now().toEpochMilliseconds() - now
                    assertTrue(timeTaken in 1000..1100, "Expected $timeTaken to be between 1000 and 1100")
                    assertEquals(
                        e.message,
                        "Database error: [SQLITE_BUSY] The database file is locked (database is locked)"
                    )
                }
            }
        }
    }

}
