package com.iv127.quizflow.server.acceptance.test.story

import com.iv127.quizflow.core.rest.api.MultipartData
import com.iv127.quizflow.core.rest.api.authorization.UsernamePasswordAuthorizationRequest
import com.iv127.quizflow.core.rest.api.question.QuestionSetVersionResponse
import com.iv127.quizflow.core.rest.api.question.QuestionsRoutes
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes
import com.iv127.quizflow.core.rest.api.quiz.QuizCreateRequest
import com.iv127.quizflow.core.rest.api.quiz.QuizQuestionResponse
import com.iv127.quizflow.core.rest.api.quiz.QuizzesRoutes
import com.iv127.quizflow.core.rest.api.user.UserCreateRequest
import com.iv127.quizflow.core.rest.api.user.UserResponse
import com.iv127.quizflow.core.rest.api.user.UsersRoutes
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import com.iv127.quizflow.server.acceptance.test.rest.impl.AuthorizationRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionSetsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuizzesRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.UsersRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.security.ApiAuthorizationTestImpl
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QuizzesTest {

    private val config: GlobalConfig = GlobalConfig.INSTANCE
    private val authorizationRoutes = AuthorizationRoutesTestImpl()
    private val questionSetsRoutes: QuestionSetsRoutes = QuestionSetsRoutesTestImpl()
    private val questionsRoutes: QuestionsRoutes = QuestionsRoutesTestImpl()
    private val usersRoutes: UsersRoutes = UsersRoutesTestImpl()
    private val quizzesRoutes: QuizzesRoutes = QuizzesRoutesTestImpl()

    @Test
    fun testCreateQuiz() = runTest {
        val (user, password) = createUser()
        val auth = authorizationRoutes.authorize(UsernamePasswordAuthorizationRequest(user.username, password))
        val questionSet =
            questionSetsRoutes.create(QuestionSetCreateRequest("Example of questionnaire", "Example of description"))

        val questionsContent = """
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

        assertThat(createdQuiz.questions)
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
    }

    private suspend fun createUser(): Pair<UserResponse, String> {
        val auth = authorizationRoutes.authorize(UsernamePasswordAuthorizationRequest("super_admin", "super_admin"))
        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        val createdUser = usersRoutes.create(ApiAuthorizationTestImpl(auth), UserCreateRequest(username, password))
        return Pair(createdUser, password)
    }

}
