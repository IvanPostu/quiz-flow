package com.iv127.quizflow.core.services.impl

import com.iv127.quizflow.core.model.question.QuestionSet
import com.iv127.quizflow.core.model.question.QuestionSetBuilder
import com.iv127.quizflow.core.services.QuestionSetService
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteTimestampUtils
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json

@OptIn(ExperimentalTime::class)
class QuestionSetServiceImpl(private val dbSupplier: () -> SqliteDatabase) : QuestionSetService {
    override fun createQuestionSet(createFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit): QuestionSet {
        val questionSet: QuestionSet = QuestionSetBuilder()
            .apply(createFunc)
            .build()

        val createdAt = SqliteTimestampUtils.toValue(Clock.System.now())
        dbSupplier().use { db ->
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            db.executeAndGetChangedRowsCount(
                """
                    INSERT INTO questions_set (
                        id,
                        version,
                        created_at,
                        archived_at,
                        json) VALUES (
                        ?, ?, ?, ?, ?);
                """.trimIndent(),
                listOf<Any?>(questionSet.id, questionSet.version, createdAt, null, Json.encodeToString(questionSet))
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
        }
        return questionSet
    }

    override fun updateQuestionSet(
        id: String,
        updateFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit
    ): QuestionSet {
        dbSupplier().use { db ->
            val createdAt = SqliteTimestampUtils.toValue(Clock.System.now())
            val updatedQuestionSet: QuestionSet = QuestionSetBuilder(selectById(id, db)!!)
                .apply(updateFunc)
                .build()
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            db.executeAndGetChangedRowsCount(
                """
                    INSERT INTO questions_set (
                        id,
                        version,
                        created_at,
                        archived_at,
                        json) VALUES (
                        ?, ?, ?, ?, ?);
                """.trimIndent(),
                listOf<Any?>(
                    updatedQuestionSet.id,
                    updatedQuestionSet.version,
                    createdAt,
                    null,
                    Json.encodeToString(updatedQuestionSet)
                )
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            return updatedQuestionSet
        }
    }

    override fun archive(id: String): QuestionSet {
        return dbSupplier().use { db ->
            val archivedAt = SqliteTimestampUtils.toValue(Clock.System.now())
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            val questionSet = selectById(id, db)!!
            db.executeAndGetChangedRowsCount(
                """
                    UPDATE questions_set SET archived_at=?
                    WHERE questions_set.id=?;
                """.trimIndent(),
                listOf(archivedAt, id)
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            questionSet
        }
    }

    override fun getQuestionSet(id: String): QuestionSet {
        val questionSet: QuestionSet = dbSupplier().use { db ->
            selectById(id, db)!!
        }
        return questionSet
    }

    override fun getQuestionSet(): List<QuestionSet> {
        return dbSupplier().use { db ->
            db.executeAndGetResultSet(
                "SELECT t.* FROM questions_set AS t"
            ).map { record ->
                val deserialized: QuestionSet = Json.decodeFromString(record["json"].toString())
                deserialized
            }
        }
    }

    private fun selectById(id: String, db: SqliteDatabase): QuestionSet? {
        val result: QuestionSet? = db.executeAndGetResultSet(
            "SELECT t.* FROM questions_set AS t WHERE t.id=? ORDER BY t.version DESC LIMIT 1;",
            listOf(id)
        )
            .map { record ->
                val deserialized: QuestionSet = Json.decodeFromString(record["json"].toString())
                return deserialized
            }.firstNotNullOfOrNull { it }
        return result
    }
}
