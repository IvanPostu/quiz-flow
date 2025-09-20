package com.iv127.quizflow.core.rest.user

import com.iv127.quizflow.core.lang.UUIDv4
import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteTimestampUtils
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named

@OptIn(ExperimentalTime::class)
class UsersRoutes(val koinApp: KoinApplication) : ApiRoute {

    private val db: () -> SqliteDatabase = {
        koinApp.koin.get<SqliteDatabase>(named("appDb"))
    }

    companion object {
        private val ROUTE_PATH: String = "/users"
    }

    override fun setup(parent: Route) {
        parent.get(ROUTE_PATH, webResponse {
            JsonWebResponse.create(list())
        })
        parent.post(ROUTE_PATH, webResponse {
            val request = call.receive<UserCreateRequest>()
            JsonWebResponse.create(create(request))
        })
    }

    private fun list(): List<UserResponse> {
        db().use { db ->
            return db.executeAndGetResultSet(
                """
                    SELECT t.id, t.username, t.json 
                    FROM users AS t;
                """.trimIndent()
            )
                .map { record ->
                    val deserialized: User = Json.decodeFromString(record["json"].toString())
                    UserResponse(deserialized.id, record["username"].toString())
                }
        }
    }

    private fun create(request: UserCreateRequest): UserResponse {
        if (request.username.isBlank()) {
            throw IllegalArgumentException("username field shouldn't be blank")
        }
        if (request.password.isBlank()) {
            throw IllegalArgumentException("password field shouldn't be blank")
        }
        db().use { db ->
            val createdAt = SqliteTimestampUtils.toValue(Clock.System.now())
            val id = UUIDv4.generate()
            val user = User(id)
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
                            '${id}',
                            '$createdAt',
                            NULL, 
                            '${request.username}',
                            '${request.password}',
                            '${Json.encodeToString(user)}'
                        );
                """
                    .trimIndent()
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            return UserResponse(id, request.username)
        }
    }
}
