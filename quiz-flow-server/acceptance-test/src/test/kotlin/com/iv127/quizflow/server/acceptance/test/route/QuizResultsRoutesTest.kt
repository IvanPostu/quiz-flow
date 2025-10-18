package com.iv127.quizflow.server.acceptance.test.route

import com.iv127.quizflow.core.rest.api.MultipartData
import com.iv127.quizflow.core.rest.api.SortOrder
import com.iv127.quizflow.core.rest.api.question.QuestionSetVersionResponse
import com.iv127.quizflow.core.rest.api.question.QuestionsRoutes
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes
import com.iv127.quizflow.core.rest.api.quiz.QuizAnswerRequest
import com.iv127.quizflow.core.rest.api.quiz.QuizCreateRequest
import com.iv127.quizflow.core.rest.api.quiz.QuizResponse
import com.iv127.quizflow.core.rest.api.quiz.QuizUpdateRequest
import com.iv127.quizflow.core.rest.api.quiz.QuizzesRoutes
import com.iv127.quizflow.core.rest.api.quizresult.QuizResultsRoutes
import com.iv127.quizflow.core.rest.api.user.UserCreateRequest
import com.iv127.quizflow.core.rest.api.user.UserResponse
import com.iv127.quizflow.core.rest.api.user.UsersRoutes
import com.iv127.quizflow.server.acceptance.test.acceptance.AuthenticationAcceptance
import com.iv127.quizflow.server.acceptance.test.rest.RestErrorException
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionSetsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuizResultsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuizzesRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.UsersRoutesTestImpl
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class QuizResultsRoutesTest {

    private val questionSetsRoutes: QuestionSetsRoutes = QuestionSetsRoutesTestImpl()
    private val questionsRoutes: QuestionsRoutes = QuestionsRoutesTestImpl()
    private val usersRoutes: UsersRoutes = UsersRoutesTestImpl()
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

    @Test
    fun testFinalizedQuizNotFoundCase() = runTest {
        val (user, password) = createUser()
        val auth = AuthenticationAcceptance.authenticateUser(user.username, password)
        val quizNotFinalized = generateFinalizedQuizzes(auth.accessToken, 1, false)[0]

        var e = assertThrows<RestErrorException> {
            quizResultsRoutes.get(auth.accessToken, quizNotFinalized.id)
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse.errorCode).isEqualTo("finalized_quiz_not_found")
        assertThat(e.restErrorResponse.message).isEqualTo("Finalized quiz was not found")
        assertThat(e.restErrorResponse.data).isEqualTo(
            mapOf(
                "quizId" to quizNotFinalized.id,
                "reason" to "quiz is not finalized",
            )
        )

        e = assertThrows<RestErrorException> {
            quizResultsRoutes.get(auth.accessToken, "blahblah1")
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse.errorCode).isEqualTo("finalized_quiz_not_found")
        assertThat(e.restErrorResponse.message).isEqualTo("Finalized quiz was not found")
        assertThat(e.restErrorResponse.data).isEqualTo(
            mapOf(
                "quizId" to "blahblah1",
                "reason" to "invalid quizId",
            )
        )
    }

    @Test
    fun testQuizResultsWithExplicitLimitOffsetAndSortOrder() = runTest {
        val (user, password) = createUser()
        val auth = AuthenticationAcceptance.authenticateUser(user.username, password)
        val quizzes = generateFinalizedQuizzes(auth.accessToken, 20)

        assertThat(quizResultsRoutes.list(auth.accessToken, 2, 5, SortOrder.ASC).map { it.quizId })
            .hasSize(5)
            .containsExactlyElementsOf(quizzes.map { it.id }.subList(2, 7))
        assertThat(quizResultsRoutes.list(auth.accessToken, 2, 5, SortOrder.DESC).map { it.quizId })
            .hasSize(5)
            .containsExactlyElementsOf(quizzes.reversed().map { it.id }.subList(2, 7))
    }

    @Test
    fun testQuizResultDefaultLimitOffsetAndSortOrder() = runTest {
        val (user, password) = createUser()
        val auth = AuthenticationAcceptance.authenticateUser(user.username, password)
        val quizzes = generateFinalizedQuizzes(auth.accessToken, 20)

        assertThat(quizResultsRoutes.list(auth.accessToken, null, null, null).map { it.quizId })
            .hasSize(10)
            .containsExactlyElementsOf(quizzes.reversed().map { it.id }.subList(0, 10))
            .isEqualTo(quizResultsRoutes.list(auth.accessToken, 0, 10, SortOrder.DESC).map { it.quizId })
    }

    private suspend fun generateFinalizedQuizzes(
        accessToken: String,
        resultsCount: Int,
        finalized: Boolean = true
    ): List<QuizResponse> {
        val questionSet =
            questionSetsRoutes.create(QuestionSetCreateRequest("Example of questionnaire", "Example of description"))

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(3)

        val quizzes = IntArray(resultsCount)
            .map {
                val createdQuiz = quizzesRoutes.create(
                    accessToken,
                    QuizCreateRequest(
                        questionSet.id, questionsSetVersion.version,
                        listOf(
                            questionsSetVersion.questions[0].id,
                            questionsSetVersion.questions[1].id,
                            questionsSetVersion.questions[2].id,
                        )
                    )
                )

                quizzesRoutes.update(
                    accessToken, createdQuiz.id, QuizUpdateRequest(
                        finalized,
                        listOf(
                            QuizAnswerRequest(questionsSetVersion.questions[0].id, listOf(1)),
                            QuizAnswerRequest(questionsSetVersion.questions[1].id, listOf(1)),
                        )
                    )
                )
            }
        return quizzes
    }

    private suspend fun createUser(): Pair<UserResponse, String> {
        val auth = AuthenticationAcceptance.authenticateSuperUser()
        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        val createdUser = usersRoutes.create(auth.accessToken, UserCreateRequest(username, password))
        return Pair(createdUser, password)
    }
}
