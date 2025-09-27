package com.iv127.quizflow.core.services.impl

import com.iv127.quizflow.core.model.question.QuestionSet
import com.iv127.quizflow.core.model.question.QuestionSetBuilder
import com.iv127.quizflow.core.model.question.QuestionSetVersion
import com.iv127.quizflow.core.services.QuestionSetService
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteTimestampUtils
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json

@OptIn(ExperimentalTime::class)
class QuestionSetServiceImpl(private val dbSupplier: () -> SqliteDatabase) : QuestionSetService {
    override fun createQuestionSet(createFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit): Pair<QuestionSet, QuestionSetVersion> {
        val (questionSet, questionSetVersion) = QuestionSetBuilder()
            .apply(createFunc)
            .buildAndIncrement()
        val createdAt = SqliteTimestampUtils.toValue(Clock.System.now())
        dbSupplier().use { db ->
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            db.executeAndGetChangedRowsCount(
                """
                    INSERT INTO question_sets (
                        id,
                        latest_version,
                        created_at,
                        archived_at,
                        json) VALUES (
                        ?, ?, ?, ?, ?);
                """.trimIndent(),
                listOf<Any?>
                    (questionSet.id, questionSet.latestVersion, createdAt, null, Json.encodeToString(questionSet))
            )
            db.executeAndGetChangedRowsCount(
                """
                    INSERT INTO question_set_versions (
                        id,
                        version,
                        created_at,
                        json) VALUES (
                        ?, ?, ?, ?);
                """.trimIndent(),
                listOf<Any?>
                    (
                    questionSetVersion.id,
                    questionSetVersion.version,
                    createdAt,
                    Json.encodeToString(questionSetVersion)
                )
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            return Pair(questionSet, questionSetVersion)
        }
    }

    override fun updateQuestionSet(
        id: String,
        updateFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit
    ): Pair<QuestionSet, QuestionSetVersion> {
        dbSupplier().use { db ->
            val createdAt = SqliteTimestampUtils.toValue(Clock.System.now())
            val (questionSet, questionSetVersion) = selectByIdAndVersionOrElseLatest(db, id, null)
            val (updatedQuestionSet, updatedQuestionSetVersion) = QuestionSetBuilder(questionSet, questionSetVersion)
                .apply(updateFunc)
                .buildAndIncrement()
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            db.executeAndGetChangedRowsCount(
                """
                    UPDATE question_sets SET latest_version=?, json=?
                    WHERE id=?;
                """.trimIndent(),
                listOf<Any?>(
                    updatedQuestionSet.latestVersion,
                    Json.encodeToString(updatedQuestionSet),
                    updatedQuestionSet.id,
                )
            )
            db.executeAndGetChangedRowsCount(
                """
                    INSERT INTO question_set_versions (
                        id,
                        version,
                        created_at,
                        json) VALUES (
                        ?, ?, ?, ?);
                """.trimIndent(),
                listOf<Any?>
                    (
                    updatedQuestionSetVersion.id,
                    updatedQuestionSetVersion.version,
                    createdAt,
                    Json.encodeToString(updatedQuestionSetVersion)
                )
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            return Pair(updatedQuestionSet, updatedQuestionSetVersion)
        }
    }

    override fun archive(id: String): QuestionSet {
        return dbSupplier().use { db ->
            val archivedAt = SqliteTimestampUtils.toValue(Clock.System.now())
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            val questionSet = selectByIdAndVersionOrElseLatest(db, id, null).first
            db.executeAndGetChangedRowsCount(
                """
                    UPDATE question_sets SET archived_at=?
                    WHERE question_sets.id=?;
                """.trimIndent(),
                listOf(archivedAt, id)
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            questionSet
        }
    }

    override fun getQuestionSet(id: String): QuestionSet {
        dbSupplier().use { db ->
            return selectByIdAndVersionOrElseLatest(db, id, null).first
        }
    }

    override fun getQuestionSetWithVersionOrElseLatest(
        id: String,
        version: Int?
    ): Pair<QuestionSet, QuestionSetVersion> {
        return dbSupplier().use { db ->
            selectByIdAndVersionOrElseLatest(db, id, version)
        }
    }

    override fun getQuestionSetList(): List<QuestionSet> {
        return dbSupplier().use { db ->
            db.executeAndGetResultSet(
                "SELECT t.* FROM question_sets AS t"
            ).map { record ->
                val deserialized: QuestionSet = Json.decodeFromString(record["json"].toString())
                deserialized
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun selectByIdAndVersionOrElseLatest(
        db: SqliteDatabase,
        id: String,
        version: Int?
    ): Pair<QuestionSet, QuestionSetVersion> {
        val questionSet: QuestionSet? = db.executeAndGetResultSet(
            "SELECT t.* FROM question_sets AS t WHERE t.id=?;",
            listOf(id)
        )
            .map { record ->
                val deserialized: QuestionSet = Json.decodeFromString(record["json"].toString())
                deserialized
            }.firstNotNullOfOrNull { it }
        if (questionSet == null) {
            throw IllegalArgumentException("Cannot find question set by id:$id")
        }
        val expectedVersion = version ?: questionSet.latestVersion
        val questionSetVersion: QuestionSetVersion? = db.executeAndGetResultSet(
            "SELECT t.* FROM question_set_versions AS t WHERE t.id=? AND t.version=?;",
            listOf(id, expectedVersion)
        )
            .map { record ->
                val deserialized: QuestionSetVersion = Json.decodeFromString(record["json"].toString())
                deserialized
            }.firstNotNullOfOrNull { it }
        if (questionSetVersion == null) {
            throw IllegalArgumentException("Cannot find question set version by id:$id and version: $expectedVersion")
        }
        return Pair(questionSet, questionSetVersion)
    }
}
