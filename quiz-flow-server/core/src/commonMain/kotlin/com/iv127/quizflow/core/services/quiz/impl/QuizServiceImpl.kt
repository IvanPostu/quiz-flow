package com.iv127.quizflow.core.services.quiz.impl

import com.iv127.quizflow.core.model.authorization.Authorization
import com.iv127.quizflow.core.model.question.Question
import com.iv127.quizflow.core.model.question.QuestionSetVersion
import com.iv127.quizflow.core.model.quizz.Quiz
import com.iv127.quizflow.core.model.quizz.QuizBuilder
import com.iv127.quizflow.core.services.quiz.QuizService
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteTimestampUtils
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json

@OptIn(ExperimentalTime::class)
class QuizServiceImpl(private val dbSupplier: () -> SqliteDatabase) : QuizService {

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
                    SqliteTimestampUtils.toValue(quiz.finalizedDate),
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
                ?: throw IllegalStateException("Can't find quiz with id $quizId")
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
                    SqliteTimestampUtils.toValue(quiz.finalizedDate),
                    Json.encodeToString(quiz),
                    quiz.id
                )
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            quiz
        }
    }

    private fun selectQuizById(db: SqliteDatabase, id: String, userId: String): Quiz? {
        val quiz: Quiz? = db.executeAndGetResultSet(
            "SELECT t.* FROM quizzes AS t WHERE t.id=? AND t.user_id=?;",
            listOf(id, userId)
        )
            .map { record ->
                val deserialized: Quiz = Json.decodeFromString(record["json"].toString())
                deserialized
            }.firstNotNullOfOrNull { it }
        return quiz
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
