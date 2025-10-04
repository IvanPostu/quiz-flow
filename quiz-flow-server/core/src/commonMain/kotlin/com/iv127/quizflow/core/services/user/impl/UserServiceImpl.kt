package com.iv127.quizflow.core.services.user.impl

import com.iv127.quizflow.core.lang.UUIDv4
import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.services.user.UserInvalidPasswordException
import com.iv127.quizflow.core.services.user.UserNotFoundException
import com.iv127.quizflow.core.services.user.UserService
import com.iv127.quizflow.core.services.user.UsernameAlreadyTakenException
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteTimestampUtils
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json

@OptIn(ExperimentalTime::class)
class UserServiceImpl(private val dbSupplier: () -> SqliteDatabase) : UserService {

    override fun getByUsernameAndPassword(username: String, password: String): User {
        dbSupplier().use { db ->
            val user = selectByUsername(db, username) ?: throw UserNotFoundException(username)
            if (user.passwordHash != password) {
                throw UserInvalidPasswordException(username)
            }
            return user
        }
    }

    override fun create(username: String, password: String): User {
        val userPersistFun: () -> User = {
            dbSupplier().use { db ->
                val createdAt = Clock.System.now()
                val createdAtSql = SqliteTimestampUtils.toValue(createdAt)
                val id = UUIDv4.generate()
                val user = User(id, username, password, createdAt, null)
                db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
                db.executeAndGetChangedRowsCount(
                    """
                    INSERT INTO users (
                        id,
                        created_at,
                        archived_at,
                        username,
                        password_hash,
                        json) VALUES 
                        (
                            ?,
                            ?,
                            ?, 
                            ?,
                            ?,
                            ?
                        );
                """.trimIndent(),
                    listOf(id, createdAtSql, null, username, password, Json.encodeToString(user))
                )
                db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
                user
            }
        }
        try {
            return userPersistFun()
        } catch (e: IllegalStateException) {
            if (e.message?.contains("UNIQUE constraint failed: users.username") == true) {
                throw UsernameAlreadyTakenException()
            }
            throw e
        }
    }

    override fun getAll(): List<User> {
        return dbSupplier().use { db ->
            db.executeAndGetResultSet(
                """
                    SELECT t.id, t.username, t.json 
                    FROM users AS t;
                """.trimIndent()
            )
                .map { record ->
                    val deserialized: User = Json.decodeFromString(record["json"].toString())
                    deserialized
                }
        }
    }

    private fun selectByUsername(
        db: SqliteDatabase,
        username: String
    ): User? {
        val user: User? = db.executeAndGetResultSet(
            "SELECT t.* FROM users AS t WHERE t.username=?;",
            listOf(username)
        )
            .map { record ->
                val deserialized: User = Json.decodeFromString(record["json"].toString())
                deserialized
            }.firstNotNullOfOrNull { it }
        return user
    }
}
