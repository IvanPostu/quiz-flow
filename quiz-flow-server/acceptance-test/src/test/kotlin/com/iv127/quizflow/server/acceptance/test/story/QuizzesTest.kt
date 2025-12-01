package com.iv127.quizflow.server.acceptance.test.story

import com.iv127.quizflow.core.rest.api.MultipartData
import com.iv127.quizflow.core.rest.api.authentication.AccessTokenResponse
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
import com.iv127.quizflow.core.rest.api.quizresult.QuizResultsRoutes
import com.iv127.quizflow.server.acceptance.test.acceptance.AuthenticationAcceptance
import com.iv127.quizflow.server.acceptance.test.acceptance.UserAcceptance
import com.iv127.quizflow.server.acceptance.test.rest.RestErrorException
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionSetsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuizResultsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuizzesRoutesTestImpl
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalTime::class)
class QuizzesTest {

    private val questionSetsRoutes: QuestionSetsRoutes = QuestionSetsRoutesTestImpl()
    private val questionsRoutes: QuestionsRoutes = QuestionsRoutesTestImpl()
    private val quizzesRoutes: QuizzesRoutes = QuizzesRoutesTestImpl()
    private val quizResultsRoutes: QuizResultsRoutes = QuizResultsRoutesTestImpl()

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

    private lateinit var auth: AccessTokenResponse

    @BeforeEach
    fun setup() = runTest {
        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        val user = UserAcceptance.createUser(username, password)
        auth = AuthenticationAcceptance.authenticateUser(user.username, password)
    }

    @Test
    fun testTakeQuizAndGetResult() = runTest {
        val randomInt = Random.nextInt()
        val questionSetName = "Example of questionnaire $randomInt"
        val questionSet =
            questionSetsRoutes.create(
                auth.accessToken,
                QuestionSetCreateRequest(questionSetName, "Example of description $randomInt")
            )

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            auth.accessToken,
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val createdQuiz = quizzesRoutes.create(
            auth.accessToken,
            QuizCreateRequest(
                questionSet.id, questionsSetVersion.version,
                listOf(
                    questionsSetVersion.questions[0].id,
                    questionsSetVersion.questions[1].id,
                    questionsSetVersion.questions[2].id,
                )
            )
        )

        val finalizedQuiz = quizzesRoutes.update(
            auth.accessToken, createdQuiz.id, QuizUpdateRequest(
                true,
                listOf(
                    QuizAnswerRequest(questionsSetVersion.questions[0].id, listOf(1)),
                    QuizAnswerRequest(questionsSetVersion.questions[1].id, listOf(1)),
                )
            )
        )

        val evaluationResult = quizResultsRoutes.get(auth.accessToken, finalizedQuiz.id)
        val evaluationResults = quizResultsRoutes.list(auth.accessToken, null, null, null, null)

        assertThat(evaluationResults).hasSize(1)

        assertThat(listOf(evaluationResult, evaluationResults[0]))
            .allSatisfy({ result ->
                assertThat(result.quizId).isEqualTo(finalizedQuiz.id)
                assertThat(result.questionSetId).isEqualTo(finalizedQuiz.questionSetId)
                assertThat(result.questionSetName).isEqualTo(questionSetName)
                assertThat(result.questionSetVersion).isEqualTo(finalizedQuiz.questionSetVersion)
                assertThat(result.quizCreatedDate).isEqualTo(finalizedQuiz.createdDate)
                assertThat(result.quizFinalizedDate).isEqualTo(finalizedQuiz.finalizedDate)
                assertThat(result.questionsCount).isEqualTo(3)
                assertThat(result.answersCount).isEqualTo(2)
                assertThat(result.correctAnswersCount).isEqualTo(1)
            })
    }


    @Test
    fun testGetQuizResultForOngoingQuiz() = runTest {
        val randomInt = Random.nextInt()
        val questionSet =
            questionSetsRoutes.create(
                auth.accessToken,
                QuestionSetCreateRequest("Example of questionnaire $randomInt", "Example of description $randomInt")
            )

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            auth.accessToken,
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val createdQuiz = quizzesRoutes.create(
            auth.accessToken,
            QuizCreateRequest(
                questionSet.id, questionsSetVersion.version,
                listOf(
                    questionsSetVersion.questions[0].id,
                    questionsSetVersion.questions[1].id,
                    questionsSetVersion.questions[2].id,
                )
            )
        )

        var updatedQuiz = quizzesRoutes.update(
            auth.accessToken, createdQuiz.id, QuizUpdateRequest(
                false,
                listOf(
                    QuizAnswerRequest(questionsSetVersion.questions[0].id, listOf(1)),
                    QuizAnswerRequest(questionsSetVersion.questions[1].id, listOf(1)),
                )
            )
        )
        var evaluationResults = quizResultsRoutes.list(auth.accessToken, null, null, null, null)
        var evaluationResult = quizResultsRoutes.get(auth.accessToken, updatedQuiz.id)

        assertThat(evaluationResults).hasSize(1)
        assertThat(listOf(evaluationResult, evaluationResults[0]))
            .allSatisfy({ result ->
                assertThat(result.quizId).isEqualTo(updatedQuiz.id)
                assertThat(result.questionSetId).isEqualTo(updatedQuiz.questionSetId)
                assertThat(result.questionSetVersion).isEqualTo(updatedQuiz.questionSetVersion)
                assertThat(result.quizCreatedDate).isEqualTo(updatedQuiz.createdDate)
                assertThat(result.quizFinalizedDate).isEqualTo(updatedQuiz.finalizedDate)
                assertThat(result.questionsCount).isEqualTo(3)
                assertThat(result.answersCount).isEqualTo(2)
                assertThat(result.correctAnswersCount).isNull()
                assertThat(result.answers)
                    .hasSize(3)
                    .anySatisfy({
                        assertThat(it.rightAnswerIndexes).isNull()
                        assertThat(it.chosenAnswerIndexes).isEqualTo(listOf(1))
                        assertThat(it.questionId).isNotNull()
                    })
                    .anySatisfy({
                        assertThat(it.rightAnswerIndexes).isNull()
                        assertThat(it.chosenAnswerIndexes).isEqualTo(listOf(1))
                        assertThat(it.questionId).isNotNull()
                    })
                    .anySatisfy({
                        assertThat(it.rightAnswerIndexes).isNull()
                        assertThat(it.chosenAnswerIndexes).isEmpty()
                        assertThat(it.questionId).isNotNull()
                    })
            })

        updatedQuiz = quizzesRoutes.update(
            auth.accessToken, createdQuiz.id, QuizUpdateRequest(
                true,
                listOf(
                    QuizAnswerRequest(questionsSetVersion.questions[0].id, listOf(1)),
                    QuizAnswerRequest(questionsSetVersion.questions[1].id, listOf(1)),
                )
            )
        )
        evaluationResults = quizResultsRoutes.list(auth.accessToken, null, null, null, null)
        evaluationResult = quizResultsRoutes.get(auth.accessToken, updatedQuiz.id)
        assertThat(evaluationResults).hasSize(1)
        assertThat(listOf(evaluationResult, evaluationResults[0]))
            .allSatisfy({ result ->
                assertThat(result.quizId).isEqualTo(updatedQuiz.id)
                assertThat(result.questionSetId).isEqualTo(updatedQuiz.questionSetId)
                assertThat(result.questionSetVersion).isEqualTo(updatedQuiz.questionSetVersion)
                assertThat(result.quizCreatedDate).isEqualTo(updatedQuiz.createdDate)
                assertThat(result.quizFinalizedDate).isEqualTo(updatedQuiz.finalizedDate)
                assertThat(result.questionsCount).isEqualTo(3)
                assertThat(result.answersCount).isEqualTo(2)
                assertThat(result.correctAnswersCount).isEqualTo(1)
                assertThat(result.answers)
                    .hasSize(3)
                    .anySatisfy({
                        assertThat(it.rightAnswerIndexes).isEqualTo(listOf(0))
                        assertThat(it.chosenAnswerIndexes).isEqualTo(listOf(1))
                        assertThat(it.questionId).isNotNull()
                    })
                    .anySatisfy({
                        assertThat(it.rightAnswerIndexes).isEqualTo(listOf(1))
                        assertThat(it.chosenAnswerIndexes).isEqualTo(listOf(1))
                        assertThat(it.questionId).isNotNull()
                    })
                    .anySatisfy({
                        assertThat(it.rightAnswerIndexes).isEqualTo(listOf(1))
                        assertThat(it.chosenAnswerIndexes).isEmpty()
                        assertThat(it.questionId).isNotNull()
                    })
            })

    }

    @Test
    fun testAttemptToSelectInvalidAnswer() = runTest {
        val questionSet =
            questionSetsRoutes.create(
                auth.accessToken,
                QuestionSetCreateRequest("Example of questionnaire", "Example of description")
            )

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            auth.accessToken,
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val createdQuiz = quizzesRoutes.create(
            auth.accessToken,
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
                auth.accessToken, createdQuiz.id, QuizUpdateRequest(
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
                auth.accessToken, createdQuiz.id, QuizUpdateRequest(
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
        val e = assertThrows<RestErrorException> {
            quizzesRoutes.update(
                auth.accessToken,
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
        val e = assertThrows<RestErrorException> {
            quizzesRoutes.get(auth.accessToken, quizId = "randomString123")
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse.errorCode).isEqualTo("quiz_not_found")
        assertThat(e.restErrorResponse.message).isEqualTo("Quiz not found")
        assertThat(e.restErrorResponse.data).isEqualTo(mapOf("quizId" to "randomString123"))
    }

    @Test
    fun testAttemptToUpdateFinalizedQuiz() = runTest {
        val questionSet =
            questionSetsRoutes.create(
                auth.accessToken,
                QuestionSetCreateRequest("Example of questionnaire", "Example of description")
            )

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            auth.accessToken,
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val createdQuiz = quizzesRoutes.create(
            auth.accessToken,
            QuizCreateRequest(
                questionSet.id, questionsSetVersion.version,
                listOf(
                    questionsSetVersion.questions[0].id,
                    questionsSetVersion.questions[1].id,
                )
            )
        )

        val updatedQuiz = quizzesRoutes.update(
            auth.accessToken, createdQuiz.id, QuizUpdateRequest(
                true,
                listOf(
                    QuizAnswerRequest(questionsSetVersion.questions[0].id, listOf(0)),
                    QuizAnswerRequest(questionsSetVersion.questions[1].id, listOf(1))
                )
            )
        )

        val e = assertThrows<RestErrorException> {
            quizzesRoutes.update(
                auth.accessToken, updatedQuiz.id, QuizUpdateRequest(
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
        val questionSet =
            questionSetsRoutes.create(
                auth.accessToken,
                QuestionSetCreateRequest("Example of questionnaire", "Example of description")
            )

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            auth.accessToken,
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val createdQuiz = quizzesRoutes.create(
            auth.accessToken,
            QuizCreateRequest(
                questionSet.id, questionsSetVersion.version,
                listOf(
                    questionsSetVersion.questions[0].id,
                    questionsSetVersion.questions[1].id,
                )
            )
        )

        val updatedQuiz = quizzesRoutes.update(
            auth.accessToken, createdQuiz.id, QuizUpdateRequest(
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
                quizzesRoutes.get(auth.accessToken, updatedQuiz.id)
            )
        ).allSatisfy({
            assertThat(it.questionSetId).isEqualTo(questionSet.id)
            assertThat(it.questionSetVersion).isEqualTo(questionsSetVersion.version)
            assertThat(it.questions)
                .hasSize(2)
                .isEqualTo(
                    listOf<QuizQuestionResponse>(
                        QuizQuestionResponse(
                            questionsSetVersion.questions[0].id,
                            questionsSetVersion.questions[0].question,
                            questionsSetVersion.questions[0].answerOptions,
                            questionsSetVersion.questions[0].correctAnswerIndexes,
                            questionsSetVersion.questions[0].correctAnswerExplanation,
                        ),
                        QuizQuestionResponse(
                            questionsSetVersion.questions[1].id,
                            questionsSetVersion.questions[1].question,
                            questionsSetVersion.questions[1].answerOptions,
                            questionsSetVersion.questions[1].correctAnswerIndexes,
                            questionsSetVersion.questions[1].correctAnswerExplanation,
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
        val questionSet =
            questionSetsRoutes.create(
                auth.accessToken,
                QuestionSetCreateRequest("Example of questionnaire", "Example of description")
            )

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            auth.accessToken,
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val createdQuiz = quizzesRoutes.create(
            auth.accessToken,
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
                quizzesRoutes.get(auth.accessToken, createdQuiz.id)
            )
        ).allSatisfy({
            assertThat(it.questionSetId).isEqualTo(questionSet.id)
            assertThat(it.questionSetVersion).isEqualTo(questionsSetVersion.version)
            assertThat(it.answers)
                .hasSize(2)
                .allSatisfy({
                    assertThat(it.questionId).isNotBlank()
                    assertThat(it.chosenAnswerIndexes).isEmpty()
                })
            assertThat(it.questions)
                .hasSize(2)
                .isEqualTo(
                    listOf<QuizQuestionResponse>(
                        QuizQuestionResponse(
                            questionsSetVersion.questions[0].id,
                            questionsSetVersion.questions[0].question,
                            questionsSetVersion.questions[0].answerOptions,
                            listOf(),
                            "",
                        ),
                        QuizQuestionResponse(
                            questionsSetVersion.questions[1].id,
                            questionsSetVersion.questions[1].question,
                            questionsSetVersion.questions[1].answerOptions,
                            listOf(),
                            "",
                        )
                    )
                )
        })

        val updatedQuiz = quizzesRoutes.update(
            auth.accessToken, createdQuiz.id, QuizUpdateRequest(
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
                quizzesRoutes.get(auth.accessToken, updatedQuiz.id)
            )
        ).allSatisfy({
            assertThat(it.questionSetId).isEqualTo(questionSet.id)
            assertThat(it.questionSetVersion).isEqualTo(questionsSetVersion.version)
            assertThat(it.questions)
                .hasSize(2)
                .isEqualTo(
                    listOf<QuizQuestionResponse>(
                        QuizQuestionResponse(
                            questionsSetVersion.questions[0].id,
                            questionsSetVersion.questions[0].question,
                            questionsSetVersion.questions[0].answerOptions,
                            listOf(),
                            "",
                        ),
                        QuizQuestionResponse(
                            questionsSetVersion.questions[1].id,
                            questionsSetVersion.questions[1].question,
                            questionsSetVersion.questions[1].answerOptions,
                            listOf(),
                            "",
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
    fun testFinalizedQuizAnswersSizeIsEqualToQuestionsSize() = runTest {
        val questionSet =
            questionSetsRoutes.create(
                auth.accessToken,
                QuestionSetCreateRequest("Example of questionnaire", "Example of description")
            )

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            auth.accessToken,
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val createdQuiz = quizzesRoutes.create(
            auth.accessToken,
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
                quizzesRoutes.get(auth.accessToken, createdQuiz.id)
            )
        ).allSatisfy({
            assertThat(it.questionSetId).isEqualTo(questionSet.id)
            assertThat(it.questionSetVersion).isEqualTo(questionsSetVersion.version)
            assertThat(it.finalizedDate).isNull()
            assertThat(it.answers)
                .hasSize(2)
                .allSatisfy({
                    assertThat(it.questionId).isNotBlank()
                    assertThat(it.chosenAnswerIndexes).isEmpty()
                })
            assertThat(it.questions)
                .hasSize(2)
                .isEqualTo(
                    listOf<QuizQuestionResponse>(
                        QuizQuestionResponse(
                            questionsSetVersion.questions[0].id,
                            questionsSetVersion.questions[0].question,
                            questionsSetVersion.questions[0].answerOptions,
                            listOf(),
                            "",
                        ),
                        QuizQuestionResponse(
                            questionsSetVersion.questions[1].id,
                            questionsSetVersion.questions[1].question,
                            questionsSetVersion.questions[1].answerOptions,
                            listOf(),
                            "",
                        )
                    )
                )
        })

        val finalizedQuiz = quizzesRoutes.update(
            auth.accessToken, createdQuiz.id, QuizUpdateRequest(
                true,
                listOf(
                    QuizAnswerRequest(questionsSetVersion.questions[1].id, listOf(1))
                )
            )
        )

        assertThat(
            listOf(
                finalizedQuiz,
                quizzesRoutes.get(auth.accessToken, finalizedQuiz.id)
            )
        ).allSatisfy({
            assertThat(it.questionSetId).isEqualTo(questionSet.id)
            assertThat(it.questionSetVersion).isEqualTo(questionsSetVersion.version)
            assertThat(it.finalizedDate).isNotNull()
            assertThat(it.questions)
                .hasSize(2)
                .isEqualTo(
                    listOf<QuizQuestionResponse>(
                        QuizQuestionResponse(
                            questionsSetVersion.questions[0].id,
                            questionsSetVersion.questions[0].question,
                            questionsSetVersion.questions[0].answerOptions,
                            questionsSetVersion.questions[0].correctAnswerIndexes,
                            questionsSetVersion.questions[0].correctAnswerExplanation,
                        ),
                        QuizQuestionResponse(
                            questionsSetVersion.questions[1].id,
                            questionsSetVersion.questions[1].question,
                            questionsSetVersion.questions[1].answerOptions,
                            questionsSetVersion.questions[1].correctAnswerIndexes,
                            questionsSetVersion.questions[1].correctAnswerExplanation,
                        )
                    )
                )
            assertThat(it.answers)
                .hasSize(2)
                .isEqualTo(
                    listOf<QuizAnswerResponse>(
                        QuizAnswerResponse(
                            questionsSetVersion.questions[0].id,
                            emptyList()
                        ),
                        QuizAnswerResponse(
                            questionsSetVersion.questions[1].id,
                            listOf(1)
                        ),
                    )
                )
        })
    }
}
