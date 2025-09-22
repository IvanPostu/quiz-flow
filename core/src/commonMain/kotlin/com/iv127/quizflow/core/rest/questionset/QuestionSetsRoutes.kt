package com.iv127.quizflow.core.rest.questionset

import com.iv127.quizflow.core.model.question.Question
import com.iv127.quizflow.core.model.question.QuestionSet
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.question.QuestionResponseMapper
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteTimestampUtils
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named

@OptIn(ExperimentalTime::class)
class QuestionSetsRoutes(val koinApp: KoinApplication) : ApiRoute {

    companion object {
        private val ROUTE_PATH: String = "/question-sets"
    }

    override fun setup(parent: Route) {
        parent.get("$ROUTE_PATH/{id}", webResponse {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("id pathParam is empty")
            JsonWebResponse.create(get(id))
        })
        parent.get(ROUTE_PATH, webResponse {
            JsonWebResponse.create(list())
        })
        parent.post(ROUTE_PATH, webResponse {
            val request = call.receive<QuestionSetCreateRequest>()
            JsonWebResponse.create(create(request))
        })
        parent.delete("$ROUTE_PATH/{id}", webResponse {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("id pathParam is empty")
            JsonWebResponse.create(archive(id))
        })
        parent.put("$ROUTE_PATH/{id}", webResponse {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("id pathParam is empty")
            val request = call.receive<QuestionSetUpdateRequest>()
            JsonWebResponse.create(update(id, request))
        })
    }

    private fun get(id: String): QuestionSetResponse {
        koinApp.koin.get<SqliteDatabase>(named("appDb")).use { db ->
            return selectById(id, db)
        }
    }

    private fun list(): List<QuestionSetResponse> {
        koinApp.koin.get<SqliteDatabase>(named("appDb")).use { db ->
            return db.executeAndGetResultSet(
                """
                    SELECT t.id, t.created_at, t.archived_at, t.json 
                    FROM questions_set AS t;
                """.trimIndent()
            )
                .map { record ->
                    val deserialized: QuestionSet = Json.decodeFromString(record["json"].toString())
                    QuestionSetResponse(
                        record["id"].toString(),
                        deserialized.name,
                        deserialized.description,
                        mapQuestionsToResponses(deserialized.questions)
                    )
                }
        }
    }

    private fun create(request: QuestionSetCreateRequest): QuestionSetResponse {
        if (request.name.isBlank()) {
            throw IllegalArgumentException("name field shouldn't be blank")
        }
        koinApp.koin.get<SqliteDatabase>(named("appDb")).use { db ->
            val createdAt = SqliteTimestampUtils.toValue(Clock.System.now())
            val questionsSet = QuestionSet("-1", request.name, request.description)
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            db.executeAndGetChangedRowsCount(
                """
                        INSERT INTO questions_set (
                            created_at,
                            archived_at,
                            json) VALUES ('$createdAt', NULL, '${Json.encodeToString(questionsSet)}');
                    """.trimIndent()
            )
            val insertedId = db.executeAndGetResultSet("SELECT last_insert_rowid() AS lastId;")[0]["lastId"]!!
            val questionsSetWithId = QuestionSet(insertedId, request.name, request.description)
            db.executeAndGetChangedRowsCount(
                """
                        UPDATE questions_set SET json='${Json.encodeToString(questionsSetWithId)}'
                        WHERE questions_set.id=$insertedId;
                    """.trimIndent()
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            return QuestionSetResponse(
                questionsSetWithId.id,
                questionsSetWithId.name,
                questionsSetWithId.description,
                mapQuestionsToResponses(questionsSetWithId.questions)
            )
        }
    }

    private fun update(id: String, request: QuestionSetUpdateRequest): QuestionSetResponse {
        koinApp.koin.get<SqliteDatabase>(named("appDb")).use { db ->
            val existing = selectById(id, db)
            val questionsSet = QuestionSet(existing.id, request.name, request.description)
            db.executeAndGetChangedRowsCount(
                """
                    UPDATE questions_set SET json='${Json.encodeToString(questionsSet)}'
                    WHERE questions_set.id=${existing.id};
                """.trimIndent()
            )
            return selectById(id, db)
        }
    }

    private fun archive(id: String): QuestionSetResponse {
        koinApp.koin.get<SqliteDatabase>(named("appDb")).use { db ->
            val existing = selectById(id, db)
            val archivedAt = SqliteTimestampUtils.toValue(Clock.System.now())
            db.executeAndGetChangedRowsCount(
                """
                    UPDATE questions_set SET archived_at='$archivedAt'
                    WHERE questions_set.id=${existing.id};
                """.trimIndent()
            )
            return selectById(id, db)
        }
    }

    private fun selectById(id: String, db: SqliteDatabase): QuestionSetResponse {
        return db.executeAndGetResultSet("SELECT t.* FROM questions_set AS t WHERE t.id=$id")
            .map { record ->
                val deserialized: QuestionSet = Json.decodeFromString(record["json"].toString())
                QuestionSetResponse(
                    record["id"].toString(),
                    deserialized.name,
                    deserialized.description,
                    mapQuestionsToResponses(deserialized.questions)
                )
            }.first()
    }

    private fun mapQuestionsToResponses(questions: List<Question>) = questions.map {
        QuestionResponseMapper.mapToResponse(it)
    }.toList()

}
