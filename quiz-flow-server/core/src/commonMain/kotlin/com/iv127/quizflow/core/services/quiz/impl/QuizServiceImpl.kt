package com.iv127.quizflow.core.services.quiz.impl

import com.iv127.quizflow.core.model.authorization.Authorization
import com.iv127.quizflow.core.model.question.Question
import com.iv127.quizflow.core.model.question.QuestionSetVersion
import com.iv127.quizflow.core.model.quizz.FinalizedQuizUpdateException
import com.iv127.quizflow.core.model.quizz.Quiz
import com.iv127.quizflow.core.model.quizz.QuizBuilder
import com.iv127.quizflow.core.model.quizz.QuizNotFoundException
import com.iv127.quizflow.core.rest.api.SortOrder
import com.iv127.quizflow.core.services.quiz.QuizService
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteTimestampUtils
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json

@OptIn(ExperimentalTime::class)
class QuizServiceImpl(private val dbSupplier: () -> SqliteDatabase) : QuizService {

    override fun getQuizList(
        authorization: Authorization,
        offset: Int,
        limit: Int,
        sortOrder: SortOrder,
        finalizedOnly: Boolean
    ): List<Quiz> {
        val userId = authorization.userId
        val isAscSortOrder = SortOrder.ASC == sortOrder
        val finalizedOnlyOrTrue = if (finalizedOnly) " finalized_at IS NOT NULL " else " 1=1 "

        return dbSupplier().use { db ->
            val offsetPrimaryKey: Int
            if (isAscSortOrder) {
                offsetPrimaryKey = db.executeAndGetResultSet(
                    """
                    SELECT IFNULL(MAX(t1.primary_key), ?) AS offset_value FROM (
                        SELECT t.primary_key 
                        FROM quizzes AS t
                        WHERE t.user_id=? AND $finalizedOnlyOrTrue
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
                        FROM quizzes AS t
                        WHERE t.user_id=? AND $finalizedOnlyOrTrue
                        ORDER BY t.primary_key DESC LIMIT ?
                    ) t1;
                    """.trimIndent(),
                    listOf(Int.MAX_VALUE, userId, offset)
                ).first()["offset_value"]!!.toInt()
            }

            db.executeAndGetResultSet(
                """
                    SELECT t.* FROM quizzes AS t 
                    WHERE t.primary_key ${if (isAscSortOrder) ">" else "<"} ? 
                        AND t.user_id=? 
                        AND $finalizedOnlyOrTrue
                    ORDER BY t.primary_key ${if (isAscSortOrder) "ASC" else "DESC"}
                    LIMIT ?;
                """.trimIndent(),
                listOf(offsetPrimaryKey, userId, limit)
            ).map { record ->
                val deserialized: Quiz = Json.decodeFromString(record["json"].toString())
                deserialized
            }
        }
    }

    override fun getQuiz(authorization: Authorization, quizId: String): Quiz {
        return dbSupplier().use { db ->
            selectQuizById(db, quizId, authorization.userId)
        }
    }

    override fun createQuiz(
        authorization: Authorization,
        questionSetVersion: QuestionSetVersion,
        questions: List<Question>
    ): Quiz {
        checkQuestionsArePartOfQuestionSet(questionSetVersion, questions)
        val quizBuilder = QuizBuilder(authorization.userId, questionSetVersion, questions)
        val quiz = quizBuilder.build()
        return dbSupplier().use { db ->
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            db.executeAndGetChangedRowsCount(
                """
                    INSERT INTO quizzes (
                        id,
                        user_id,
                        question_set_id,
                        question_set_version,
                        created_at,
                        finalized_at,
                        json) VALUES (
                        ?, ?, ?, ?, ?, ?, ?);
                """.trimIndent(),
                listOf<Any?>
                    (
                    quiz.id,
                    quiz.userId,
                    quiz.questionSetId,
                    quiz.questionSetVersion,
                    SqliteTimestampUtils.toValue(quiz.createdDate),
                    if (quiz.finalizedDate != null) SqliteTimestampUtils.toValue(quiz.finalizedDate) else null,
                    Json.encodeToString(quiz)
                )
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            quiz
        }
    }

    override fun updateQuiz(
        authorization: Authorization,
        quizId: String,
        questionSetVersion: QuestionSetVersion,
        updateFunc: (quizBuilder: QuizBuilder) -> Unit
    ): Quiz {
        return dbSupplier().use { db ->
            val selectedQuiz = selectQuizById(db, quizId, authorization.userId)

            if (selectedQuiz.isFinalized()) {
                throw FinalizedQuizUpdateException(quizId)
            }

            val quizBuilder = QuizBuilder(selectedQuiz, questionSetVersion)
            updateFunc(quizBuilder)
            val quiz = quizBuilder.build()

            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            db.executeAndGetChangedRowsCount(
                """
                    UPDATE quizzes SET
                        finalized_at=?,
                        json=?
                    WHERE id=?;
                """.trimIndent(),
                listOf<Any?>
                    (
                    if (quiz.finalizedDate != null) SqliteTimestampUtils.toValue(quiz.finalizedDate) else null,
                    Json.encodeToString(quiz),
                    quiz.id
                )
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            quiz
        }
    }

    private fun selectQuizById(db: SqliteDatabase, id: String, userId: String): Quiz {
        val quiz: Quiz? = db.executeAndGetResultSet(
            "SELECT t.* FROM quizzes AS t WHERE t.id=? AND t.user_id=?;",
            listOf(id, userId)
        )
            .map { record ->
                val deserialized: Quiz = Json.decodeFromString(record["json"].toString())
                deserialized
            }.firstNotNullOfOrNull { it }
        return quiz ?: throw QuizNotFoundException(id)
    }

    private fun checkQuestionsArePartOfQuestionSet(
        questionSetVersion: QuestionSetVersion,
        questions: List<Question>
    ) {
        val unknownQuestions = questions.filter { question ->
            questionSetVersion.questions.none {
                it.id == question.id
            }
        }
        if (unknownQuestions.isNotEmpty()) {
            throw IllegalStateException("Questions that are not part of question set found: $unknownQuestions")
        }
    }

}
