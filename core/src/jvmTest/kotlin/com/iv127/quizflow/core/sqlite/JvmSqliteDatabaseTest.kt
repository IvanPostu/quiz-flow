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
    fun testSelect() {
        JvmSqliteDatabase(pathToFile).use { sqliteDatabase ->
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
        JvmSqliteDatabase(pathToFile).use { sqlite1 ->
            JvmSqliteDatabase(pathToFile).use { sqlite2 ->
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
                    val timeTaken = Clock.System.now().toEpochMilliseconds() - now
                    assertTrue(timeTaken >= 800)
                    assertEquals(
                        e.message,
                        "Database error: [SQLITE_BUSY] The database file is locked (database is locked)"
                    )
                }
            }
        }

    }

}
