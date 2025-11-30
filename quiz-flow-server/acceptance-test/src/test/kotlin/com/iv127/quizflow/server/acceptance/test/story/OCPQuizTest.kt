package com.iv127.quizflow.server.acceptance.test.story

import com.iv127.quizflow.core.rest.api.MultipartData
import com.iv127.quizflow.core.rest.api.authentication.AccessTokenResponse
import com.iv127.quizflow.core.rest.api.question.QuestionSetVersionResponse
import com.iv127.quizflow.core.rest.api.question.QuestionsRoutes
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes
import com.iv127.quizflow.core.rest.api.quiz.QuizzesRoutes
import com.iv127.quizflow.core.rest.api.quizresult.QuizResultsRoutes
import com.iv127.quizflow.server.acceptance.test.acceptance.AuthenticationAcceptance
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionSetsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuizResultsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuizzesRoutesTestImpl
import java.io.File
import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assumptions.assumeThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalTime::class)
class OCPQuizTest {
    private val questionSetsRoutes: QuestionSetsRoutes = QuestionSetsRoutesTestImpl()
    private val questionsRoutes: QuestionsRoutes = QuestionsRoutesTestImpl()
    private val quizzesRoutes: QuizzesRoutes = QuizzesRoutesTestImpl()
    private val quizResultsRoutes: QuizResultsRoutes = QuizResultsRoutesTestImpl()

    private lateinit var auth: AccessTokenResponse
    private lateinit var homeDirectory: String

    @BeforeEach
    fun setup() = runTest {
        val username = "super_admin"
        val password = "super_admin"
        auth = AuthenticationAcceptance.authenticateUser(username, password)
        homeDirectory = System.getProperty("user.home")

        assumeThat(homeDirectory)
            .isNotBlank()
    }

    @Disabled
    @RepeatedTest(26)
    fun `test take a quiz and get result`() = runTest {
        val randomInt = Random.nextUInt()
        val questionSet =
            questionSetsRoutes.create(
                auth.accessToken,
                QuestionSetCreateRequest("OCP Test $randomInt", "OCP Test Description $randomInt")
            )

        val file = File("$homeDirectory/Projects/docubase-documents/Java/OCP21/OCP21.MD")
        assumeThat(file).exists()
        val questionsContent: String = file.readText()

        val questionsSetVersion: QuestionSetVersionResponse = questionsRoutes.upload(
            auth.accessToken,
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent.encodeToByteArray(), null)),
            questionSet.id
        )
        assertThat(questionsSetVersion.questions).hasSize(366)
    }

}
