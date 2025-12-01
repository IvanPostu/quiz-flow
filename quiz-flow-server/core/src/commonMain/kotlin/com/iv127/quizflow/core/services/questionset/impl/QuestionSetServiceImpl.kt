package com.iv127.quizflow.core.services.questionset.impl

import com.iv127.quizflow.core.model.question.InvalidQuestionSetVersionException
import com.iv127.quizflow.core.model.question.QuestionSet
import com.iv127.quizflow.core.model.question.QuestionSetBuilder
import com.iv127.quizflow.core.model.question.QuestionSetNotFoundException
import com.iv127.quizflow.core.model.question.QuestionSetVersion
import com.iv127.quizflow.core.rest.api.SortOrder
import com.iv127.quizflow.core.services.authentication.AuthenticationService
import com.iv127.quizflow.core.services.questionset.QuestionSetService
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteTimestampUtils
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json

@OptIn(ExperimentalTime::class)
class QuestionSetServiceImpl(private val dbSupplier: () -> SqliteDatabase) : QuestionSetService {
    override fun createQuestionSet(
        userId: String,
        createFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit
    ): Pair<QuestionSet, QuestionSetVersion> {
        val (questionSet, questionSetVersion) = QuestionSetBuilder(userId)
            .apply(createFunc)
            .buildAndIncrement()
        dbSupplier().use { db ->
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            db.executeAndGetChangedRowsCount(
                """
                    INSERT INTO question_sets (
                        id,
                        user_id,
                        latest_version,
                        created_at,
                        archived_at,
                        json) VALUES (
                        ?, ?, ?, ?, ?, ?);
                """.trimIndent(),
                listOf<Any?>
                    (
                    questionSet.id,
                    questionSet.userId,
                    questionSet.latestVersion,
                    SqliteTimestampUtils.toValue(questionSet.createdDate),
                    null,
                    Json.encodeToString(questionSet)
                )
            )
            val createdAt = SqliteTimestampUtils.toValue(Clock.System.now())
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
        userId: String,
        id: String,
        updateFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit
    ): Pair<QuestionSet, QuestionSetVersion> {
        dbSupplier().use { db ->
            val createdAt = SqliteTimestampUtils.toValue(Clock.System.now())
            val (questionSet, questionSetVersion) = selectByIdAndVersionOrElseLatest(db, id, null)
            if (questionSet.userId != userId) {
                throw QuestionSetNotFoundException(id)
            }
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

    override fun archive(userId: String, id: String): QuestionSet {
        return dbSupplier().use { db ->
            val archivedAt = SqliteTimestampUtils.toValue(Clock.System.now())
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            val questionSet = selectByIdAndVersionOrElseLatest(db, id, null).first
            if (questionSet.userId != userId) {
                throw QuestionSetNotFoundException(id)
            }
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

    override fun getQuestionSetWithVersionOrElseLatest(
        userId: String,
        id: String,
        version: Int?
    ): Pair<QuestionSet, QuestionSetVersion> {
        return dbSupplier().use { db ->
            val pair = selectByIdAndVersionOrElseLatest(db, id, version)
            if (pair.first.userId != userId && pair.first.userId != AuthenticationService.SUPER_USER_ID) {
                throw QuestionSetNotFoundException(id)
            }
            pair
        }
    }

    override fun getQuestionSetList(
        userId: String,
        limit: Int,
        offset: Int,
        sortOrder: SortOrder
    ): List<QuestionSet> {
        val isAscSortOrder = SortOrder.ASC == sortOrder
        return dbSupplier().use { db ->
            val offsetPrimaryKey: Int
            if (isAscSortOrder) {
                offsetPrimaryKey = db.executeAndGetResultSet(
                    """
                    SELECT IFNULL(MAX(t1.primary_key), ?) AS offset_value FROM (
                        SELECT t.primary_key 
                        FROM question_sets AS t 
                        WHERE t.user_id=?
                        ORDER BY t.primary_key ASC LIMIT ?
                    ) t1;
                    """.trimIndent(),
                    listOf(0, userId, offset)
                ).first()["offset_value"]!!.toInt()
            } else {
                offsetPrimaryKey = db.executeAndGetResultSet(
                    """
                    SELECT IFNULL(MIN(t1.primary_key), ?) AS offset_value FROM (
                        SELECT t.primary_key 
                        FROM question_sets AS t 
                        WHERE t.user_id=?
                        ORDER BY t.primary_key DESC LIMIT ?
                    ) t1;
                    """.trimIndent(),
                    listOf(Int.MAX_VALUE, userId, offset)
                ).first()["offset_value"]!!.toInt()
            }


            db.executeAndGetResultSet(
                """
                    SELECT t.* FROM question_sets AS t 
                    WHERE t.user_id=? 
                        AND t.primary_key ${if (isAscSortOrder) ">" else "<"} ?
                    ORDER BY t.primary_key ${if (isAscSortOrder) "ASC" else "DESC"}
                    LIMIT ?;
                """.trimIndent(),
                listOf(userId, offsetPrimaryKey, limit)
            ).map { record ->
                val deserialized: QuestionSet = Json.decodeFromString(record["json"].toString())
                deserialized
            }
        }
    }

    override fun getQuestionSetsByIds(userId: String, questionSetIds: List<String>): Map<String, QuestionSet> {
        val result = mutableMapOf<String, QuestionSet>()
        dbSupplier().use { db ->
            db.executeAndGetResultSet(
                """
                    SELECT t.* FROM question_sets AS t 
                    WHERE t.id IN (${questionSetIds.map { "?" }.joinToString(separator = ",")});
                """.trimIndent(),
                questionSetIds
            ).map { record ->
                val deserialized: QuestionSet = Json.decodeFromString(record["json"].toString())
                deserialized
            }.forEach {
                result[it.id] = it
            }
        }
        return result
    }

    @Throws(QuestionSetNotFoundException::class, InvalidQuestionSetVersionException::class)
    private fun selectByIdAndVersionOrElseLatest(
        db: SqliteDatabase,
        id: String,
        version: Int?
    ): Pair<QuestionSet, QuestionSetVersion> {
        val questionSet: QuestionSet = selectQuestionSet(db, id)
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
            throw InvalidQuestionSetVersionException(id, expectedVersion)
        }
        return Pair(questionSet, questionSetVersion)
    }

    @Throws(QuestionSetNotFoundException::class)
    private fun selectQuestionSet(
        db: SqliteDatabase,
        id: String,
    ): QuestionSet {
        val questionSet: QuestionSet? = db.executeAndGetResultSet(
            "SELECT t.* FROM question_sets AS t WHERE t.id=?;",
            listOf(id)
        )
            .map { record ->
                val deserialized: QuestionSet = Json.decodeFromString(record["json"].toString())
                deserialized
            }.firstNotNullOfOrNull { it }
        if (questionSet == null) {
            throw QuestionSetNotFoundException(id)
        }
        return questionSet
    }
}
