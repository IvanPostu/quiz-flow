package com.iv127.quizflow.server.acceptance.test.story

import com.iv127.quizflow.core.rest.api.MultipartData
import com.iv127.quizflow.core.rest.api.authorization.UsernamePasswordAuthorizationRequest
import com.iv127.quizflow.core.rest.api.question.QuestionSetVersionResponse
import com.iv127.quizflow.core.rest.api.question.QuestionsRoutes
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes
import com.iv127.quizflow.core.rest.api.quiz.QuizAnswerRequest
import com.iv127.quizflow.core.rest.api.quiz.QuizAnswerResponse
import com.iv127.quizflow.core.rest.api.quiz.QuizCreateRequest
import com.iv127.quizflow.core.rest.api.quiz.QuizQuestionResponse
import com.iv127.quizflow.core.rest.api.quiz.QuizUpdateRequest
import com.iv127.quizflow.core.rest.api.quiz.QuizzesRoutes
import com.iv127.quizflow.core.rest.api.user.UserCreateRequest
import com.iv127.quizflow.core.rest.api.user.UserResponse
import com.iv127.quizflow.core.rest.api.user.UsersRoutes
import com.iv127.quizflow.server.acceptance.test.rest.RestErrorException
import com.iv127.quizflow.server.acceptance.test.rest.impl.AuthorizationsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionSetsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuizzesRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.UsersRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.security.ApiAuthorizationTestImpl
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class QuizzesTest {

    private val authorizationRoutes = AuthorizationsRoutesTestImpl()
    private val questionSetsRoutes: QuestionSetsRoutes = QuestionSetsRoutesTestImpl()
    private val questionsRoutes: QuestionsRoutes = QuestionsRoutesTestImpl()
    private val usersRoutes: UsersRoutes = UsersRoutesTestImpl()
    private val quizzesRoutes: QuizzesRoutes = QuizzesRoutesTestImpl()

    private val questionsContent = """
            ```
            1. Question1?

            A. 1 test 1
            B. 2 test 1

            A. The right answer
            The right answers
            ```
            ```
            1. Question2?

            A. 1 test 2
            B. 2 test 2

            B. The right answer
            The right answers
            ```
            ```
            1. Question3?

            A. 1 test 3
            B. 2 test 3

            B. The right answer
            The right answers
            ```
        """.trimIndent().encodeToByteArray()

    @Test
    fun testAttemptToSelectInvalidAnswer() = runTest {
        val (user, password) = createUser()
        val auth = authorizationRoutes.authorize(UsernamePasswordAuthorizationRequest(user.username, password))
        val questionSet =
            questionSetsRoutes.create(QuestionSetCreateRequest("Example of questionnaire", "Example of description"))

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val createdQuiz = quizzesRoutes.create(
            ApiAuthorizationTestImpl(auth),
            QuizCreateRequest(
                questionSet.id, questionsSetVersion.version,
                listOf(
                    questionsSetVersion.questions[0].id,
                    questionsSetVersion.questions[1].id,
                )
            )
        )

        var e = assertThrows<RestErrorException> {
            quizzesRoutes.update(
                ApiAuthorizationTestImpl(auth), createdQuiz.id, QuizUpdateRequest(
                    true,
                    listOf(
                        QuizAnswerRequest("invalidId", listOf(0)),
                    )
                )
            )
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse.errorCode).isEqualTo("invalid_quiz_answer")
        assertThat(e.restErrorResponse.message).isEqualTo("Quiz answer is invalid")
        assertThat(e.restErrorResponse.data).isEqualTo(mapOf("message" to "Answer with id: invalidId is not found"))

        val answerId = questionsSetVersion.questions[0].id
        e = assertThrows<RestErrorException> {
            quizzesRoutes.update(
                ApiAuthorizationTestImpl(auth), createdQuiz.id, QuizUpdateRequest(
                    true,
                    listOf(
                        QuizAnswerRequest(answerId, listOf(2, 10, -1, 0)),
                    )
                )
            )
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse.errorCode).isEqualTo("invalid_quiz_answer")
        assertThat(e.restErrorResponse.message).isEqualTo("Quiz answer is invalid")
        assertThat(e.restErrorResponse.data).isEqualTo(
            mapOf(
                "message" to
                    "Answer with id: $answerId has unknown answer indexes: [2, 10, -1]"
            )
        )
    }

    @Test
    fun testUpdateQuizByRandomId() = runTest {
        val (user, password) = createUser()
        val auth = authorizationRoutes.authorize(UsernamePasswordAuthorizationRequest(user.username, password))


        val e = assertThrows<RestErrorException> {
            quizzesRoutes.update(
                ApiAuthorizationTestImpl(auth),
                quizId = "randomString123",
                QuizUpdateRequest(false, listOf())
            )
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse.errorCode).isEqualTo("quiz_not_found")
        assertThat(e.restErrorResponse.message).isEqualTo("Quiz not found")
        assertThat(e.restErrorResponse.data).isEqualTo(mapOf("quizId" to "randomString123"))
    }

    @Test
    fun testGetQuizByRandomId() = runTest {
        val (user, password) = createUser()
        val auth = authorizationRoutes.authorize(UsernamePasswordAuthorizationRequest(user.username, password))


        val e = assertThrows<RestErrorException> {
            quizzesRoutes.get(ApiAuthorizationTestImpl(auth), quizId = "randomString123")
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse.errorCode).isEqualTo("quiz_not_found")
        assertThat(e.restErrorResponse.message).isEqualTo("Quiz not found")
        assertThat(e.restErrorResponse.data).isEqualTo(mapOf("quizId" to "randomString123"))
    }

    @Test
    fun testAttemptToUpdateFinalizedQuiz() = runTest {
        val (user, password) = createUser()
        val auth = authorizationRoutes.authorize(UsernamePasswordAuthorizationRequest(user.username, password))
        val questionSet =
            questionSetsRoutes.create(QuestionSetCreateRequest("Example of questionnaire", "Example of description"))

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val createdQuiz = quizzesRoutes.create(
            ApiAuthorizationTestImpl(auth),
            QuizCreateRequest(
                questionSet.id, questionsSetVersion.version,
                listOf(
                    questionsSetVersion.questions[0].id,
                    questionsSetVersion.questions[1].id,
                )
            )
        )

        val updatedQuiz = quizzesRoutes.update(
            ApiAuthorizationTestImpl(auth), createdQuiz.id, QuizUpdateRequest(
                true,
                listOf(
                    QuizAnswerRequest(questionsSetVersion.questions[0].id, listOf(0)),
                    QuizAnswerRequest(questionsSetVersion.questions[1].id, listOf(1))
                )
            )
        )

        val e = assertThrows<RestErrorException> {
            quizzesRoutes.update(
                ApiAuthorizationTestImpl(auth), updatedQuiz.id, QuizUpdateRequest(
                    true,
                    listOf(
                        QuizAnswerRequest(questionsSetVersion.questions[0].id, listOf(0)),
                        QuizAnswerRequest(questionsSetVersion.questions[1].id, listOf(1))
                    )
                )
            )
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse.errorCode).isEqualTo("finalized_question_update")
        assertThat(e.restErrorResponse.message).isEqualTo("Finalized question update is not allowed")
        assertThat(e.restErrorResponse.data).isEqualTo(mapOf("quizId" to createdQuiz.id))
    }

    @Test
    fun testFinalizeQuiz() = runTest {
        val (user, password) = createUser()
        val auth = authorizationRoutes.authorize(UsernamePasswordAuthorizationRequest(user.username, password))
        val questionSet =
            questionSetsRoutes.create(QuestionSetCreateRequest("Example of questionnaire", "Example of description"))

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val createdQuiz = quizzesRoutes.create(
            ApiAuthorizationTestImpl(auth),
            QuizCreateRequest(
                questionSet.id, questionsSetVersion.version,
                listOf(
                    questionsSetVersion.questions[0].id,
                    questionsSetVersion.questions[1].id,
                )
            )
        )

        val updatedQuiz = quizzesRoutes.update(
            ApiAuthorizationTestImpl(auth), createdQuiz.id, QuizUpdateRequest(
                true,
                listOf(
                    QuizAnswerRequest(questionsSetVersion.questions[0].id, listOf(0)),
                    QuizAnswerRequest(questionsSetVersion.questions[1].id, listOf(1))
                )
            )
        )

        assertThat(
            listOf(
                updatedQuiz,
                quizzesRoutes.get(ApiAuthorizationTestImpl(auth), updatedQuiz.id)
            )
        ).allSatisfy({
            assertThat(it.questionSetId).isEqualTo(questionSet.id)
            assertThat(it.questionSetVersion).isEqualTo(questionsSetVersion.version)
            assertThat(it.isFinalized).isTrue()
            assertThat(it.questions)
                .hasSize(2)
                .isEqualTo(
                    listOf<QuizQuestionResponse>(
                        QuizQuestionResponse(
                            questionsSetVersion.questions[0].id,
                            questionsSetVersion.questions[0].question,
                            questionsSetVersion.questions[0].answerOptions
                        ),
                        QuizQuestionResponse(
                            questionsSetVersion.questions[1].id,
                            questionsSetVersion.questions[1].question,
                            questionsSetVersion.questions[1].answerOptions
                        )
                    )
                )
            assertThat(it.answers)
                .hasSize(2)
                .isEqualTo(
                    listOf<QuizAnswerResponse>(
                        QuizAnswerResponse(
                            questionsSetVersion.questions[0].id,
                            listOf(0)
                        ),
                        QuizAnswerResponse(
                            questionsSetVersion.questions[1].id,
                            listOf(1)
                        ),
                    )
                )
        })
    }

    @Test
    fun testCreateAndUpdateQuiz() = runTest {
        val (user, password) = createUser()
        val auth = authorizationRoutes.authorize(UsernamePasswordAuthorizationRequest(user.username, password))
        val questionSet =
            questionSetsRoutes.create(QuestionSetCreateRequest("Example of questionnaire", "Example of description"))

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val createdQuiz = quizzesRoutes.create(
            ApiAuthorizationTestImpl(auth),
            QuizCreateRequest(
                questionSet.id, questionsSetVersion.version,
                listOf(
                    questionsSetVersion.questions[0].id,
                    questionsSetVersion.questions[1].id,
                )
            )
        )

        assertThat(
            listOf(
                createdQuiz,
                quizzesRoutes.get(ApiAuthorizationTestImpl(auth), createdQuiz.id)
            )
        ).allSatisfy({
            assertThat(it.questionSetId).isEqualTo(questionSet.id)
            assertThat(it.questionSetVersion).isEqualTo(questionsSetVersion.version)
            assertThat(it.answers).isEmpty()
            assertThat(it.isFinalized).isFalse()
            assertThat(it.questions)
                .hasSize(2)
                .isEqualTo(
                    listOf<QuizQuestionResponse>(
                        QuizQuestionResponse(
                            questionsSetVersion.questions[0].id,
                            questionsSetVersion.questions[0].question,
                            questionsSetVersion.questions[0].answerOptions
                        ),
                        QuizQuestionResponse(
                            questionsSetVersion.questions[1].id,
                            questionsSetVersion.questions[1].question,
                            questionsSetVersion.questions[1].answerOptions
                        )
                    )
                )
        })

        val updatedQuiz = quizzesRoutes.update(
            ApiAuthorizationTestImpl(auth), createdQuiz.id, QuizUpdateRequest(
                false,
                listOf(
                    QuizAnswerRequest(questionsSetVersion.questions[0].id, listOf(0)),
                    QuizAnswerRequest(questionsSetVersion.questions[1].id, listOf(1))
                )
            )
        )

        assertThat(
            listOf(
                updatedQuiz,
                quizzesRoutes.get(ApiAuthorizationTestImpl(auth), updatedQuiz.id)
            )
        ).allSatisfy({
            assertThat(it.questionSetId).isEqualTo(questionSet.id)
            assertThat(it.questionSetVersion).isEqualTo(questionsSetVersion.version)
            assertThat(it.isFinalized).isFalse()
            assertThat(it.questions)
                .hasSize(2)
                .isEqualTo(
                    listOf<QuizQuestionResponse>(
                        QuizQuestionResponse(
                            questionsSetVersion.questions[0].id,
                            questionsSetVersion.questions[0].question,
                            questionsSetVersion.questions[0].answerOptions
                        ),
                        QuizQuestionResponse(
                            questionsSetVersion.questions[1].id,
                            questionsSetVersion.questions[1].question,
                            questionsSetVersion.questions[1].answerOptions
                        )
                    )
                )
            assertThat(it.answers)
                .hasSize(2)
                .isEqualTo(
                    listOf<QuizAnswerResponse>(
                        QuizAnswerResponse(
                            questionsSetVersion.questions[0].id,
                            listOf(0)
                        ),
                        QuizAnswerResponse(
                            questionsSetVersion.questions[1].id,
                            listOf(1)
                        ),
                    )
                )
        })
    }

    private suspend fun createUser(): Pair<UserResponse, String> {
        val auth = authorizationRoutes.authorize(UsernamePasswordAuthorizationRequest("super_admin", "super_admin"))
        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        val createdUser = usersRoutes.create(ApiAuthorizationTestImpl(auth), UserCreateRequest(username, password))
        return Pair(createdUser, password)
    }

}
