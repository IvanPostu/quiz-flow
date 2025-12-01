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
import com.iv127.quizflow.core.rest.api.quizresult.QuizFinalizedStateType
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
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalTime::class)
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

        val e = assertThrows<RestErrorException> {
            quizResultsRoutes.get(auth.accessToken, "blahblah1")
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse.errorCode).isEqualTo("quiz_not_found")
        assertThat(e.restErrorResponse.message).isEqualTo("Quiz not found")
        assertThat(e.restErrorResponse.data).isEqualTo(
            mapOf(
                "quizId" to "blahblah1"
            )
        )
    }

    @Test
    fun testQuizResultsWithExplicitLimitOffsetAndSortOrder() = runTest {
        val (user, password) = createUser()
        val auth = AuthenticationAcceptance.authenticateUser(user.username, password)
        val quizzes = generateFinalizedQuizzes(auth.accessToken, 20)

        assertThat(quizResultsRoutes.list(auth.accessToken, 2, 5, SortOrder.ASC, null).map { it.quizId })
            .hasSize(5)
            .containsExactlyElementsOf(quizzes.map { it.id }.subList(2, 7))
        assertThat(quizResultsRoutes.list(auth.accessToken, 2, 5, SortOrder.DESC, null).map { it.quizId })
            .hasSize(5)
            .containsExactlyElementsOf(quizzes.reversed().map { it.id }.subList(2, 7))
    }

    @Test
    fun testQuizResultDefaultLimitOffsetAndSortOrder() = runTest {
        val (user, password) = createUser()
        val auth = AuthenticationAcceptance.authenticateUser(user.username, password)
        val quizzes = generateFinalizedQuizzes(auth.accessToken, 20)

        assertThat(quizResultsRoutes.list(auth.accessToken, null, null, null, null).map { it.quizId })
            .hasSize(10)
            .containsExactlyElementsOf(quizzes.reversed().map { it.id }.subList(0, 10))
            .isEqualTo(quizResultsRoutes.list(auth.accessToken, 0, 10, SortOrder.DESC, null).map { it.quizId })
    }

    @Test
    fun testQuizResultsWithDifferentStates() = runTest(timeout = 300.seconds) {
        val (user, password) = createUser()
        val auth = AuthenticationAcceptance.authenticateUser(user.username, password)
        val finalizedQuizzes = generateFinalizedQuizzes(auth.accessToken, 3)
        val nonFinalizedQuizzes = generateNonFinalizedQuizzes(auth.accessToken, 3)

        assertThat(finalizedQuizzes).allSatisfy({
            assertThat(it.finalizedDate).isNotNull()
        })
        assertThat(nonFinalizedQuizzes).allSatisfy({
            assertThat(it.finalizedDate).isNull()
        })

        assertThat(quizResultsRoutes.list(auth.accessToken, null, null, null, null).map { it.quizId })
            .hasSize(6)
            .isEqualTo((finalizedQuizzes + nonFinalizedQuizzes).map { it.id }.reversed())
            .isEqualTo(quizResultsRoutes.list(auth.accessToken, null, null, SortOrder.DESC, null).map { it.quizId })
            .isEqualTo(
                quizResultsRoutes.list(auth.accessToken, null, null, null, QuizFinalizedStateType.ALL)
                    .map { it.quizId }
            )
            .isEqualTo(
                quizResultsRoutes.list(auth.accessToken, null, null, SortOrder.DESC, QuizFinalizedStateType.ALL)
                    .map { it.quizId })

        assertThat(
            quizResultsRoutes.list(auth.accessToken, null, null, null, QuizFinalizedStateType.FINALIZED_ONLY)
                .map { it.quizId })
            .hasSize(3)
            .isEqualTo(finalizedQuizzes.map { it.id }.reversed())
            .isEqualTo(
                quizResultsRoutes.list(
                    auth.accessToken,
                    null,
                    null,
                    SortOrder.DESC,
                    QuizFinalizedStateType.FINALIZED_ONLY
                )
                    .map { it.quizId })
        assertThat(
            quizResultsRoutes.list(auth.accessToken, null, null, null, QuizFinalizedStateType.NON_FINALIZED_ONLY)
                .map { it.quizId })
            .hasSize(3)
            .isEqualTo(nonFinalizedQuizzes.map { it.id }.reversed())
            .isEqualTo(
                quizResultsRoutes.list(
                    auth.accessToken,
                    null,
                    null,
                    SortOrder.DESC,
                    QuizFinalizedStateType.NON_FINALIZED_ONLY
                )
                    .map { it.quizId })
    }

    private suspend fun generateNonFinalizedQuizzes(
        accessToken: String,
        resultsCount: Int,
    ): List<QuizResponse> {
        return generateQuizzes(accessToken, resultsCount, false)
    }

    private suspend fun generateFinalizedQuizzes(
        accessToken: String,
        resultsCount: Int,
    ): List<QuizResponse> {
        return generateQuizzes(accessToken, resultsCount, true)
    }

    private suspend fun generateQuizzes(
        accessToken: String,
        resultsCount: Int,
        finalized: Boolean
    ): List<QuizResponse> {
        val questionSet =
            questionSetsRoutes.create(
                accessToken,
                QuestionSetCreateRequest("Example of questionnaire", "Example of description")
            )

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            accessToken,
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
