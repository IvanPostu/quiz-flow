package com.iv127.quizflow.server.acceptance.test.story

import com.iv127.quizflow.core.rest.api.MultipartData
import com.iv127.quizflow.core.rest.api.authentication.AccessTokenResponse
import com.iv127.quizflow.core.rest.api.question.QuestionResponse
import com.iv127.quizflow.core.rest.api.question.QuestionsRoutes
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import com.iv127.quizflow.server.acceptance.test.acceptance.AuthenticationAcceptance
import com.iv127.quizflow.server.acceptance.test.acceptance.UserAcceptance
import com.iv127.quizflow.server.acceptance.test.rest.RestErrorException
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionSetsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionsRoutesTestImpl
import kotlin.random.Random
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class QuestionSetsTest {

    private val config: GlobalConfig = GlobalConfig.INSTANCE
    private val questionSetsRoutes: QuestionSetsRoutes = QuestionSetsRoutesTestImpl()
    private val questionsRoutes: QuestionsRoutes = QuestionsRoutesTestImpl()

    private lateinit var auth: AccessTokenResponse

    @BeforeEach
    fun setup() = runTest {
        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        val user = UserAcceptance.createUser(username, password)
        auth = AuthenticationAcceptance.authenticateUser(user.username, password)
    }

    @Test
    fun testGetQuestionSetVersionByInvalidId() = runTest {
        val createRequest = QuestionSetCreateRequest("Example of questionnaire", "Example of description")
        val created = questionSetsRoutes.create(auth.accessToken, createRequest)
        val invalidId = created.id + "aaa"

        questionsRoutes.getQuestionSetVersion(created.id)
        val callbacks = listOf<suspend () -> Unit>(
            { questionsRoutes.getQuestionSetVersion(invalidId) },
            { questionsRoutes.getQuestionSetVersion(invalidId, 1) },
            { questionsRoutes.getQuestionSetVersion(invalidId, -1) },
        )

        for (callback in callbacks) {
            val e = assertThrows<RestErrorException> {
                callback()
            }
            assertThat(e.httpStatusCode).isEqualTo(400)
            assertThat(e.restErrorResponse).satisfies({
                assertThat(it.uniqueId).isNotBlank()
                assertThat(it.errorCode).isEqualTo("question_set_not_found")
                assertThat(it.message).isEqualTo("Question set not found")
                assertThat(it.data).isEqualTo(
                    mapOf(
                        "questionSetId" to invalidId,
                    )
                )
            })
        }
    }

    @Test
    fun testQuestionSetInvalidVersions() = runTest {
        val createRequest = QuestionSetCreateRequest("Example of questionnaire", "Example of description")
        val created = questionSetsRoutes.create(auth.accessToken, createRequest)

        questionsRoutes.getQuestionSetVersion(created.id)
        val e = assertThrows<RestErrorException> {
            questionsRoutes.getQuestionSetVersion(created.id, -1)
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("invalid_question_set_version")
            assertThat(it.message).isEqualTo("Invalid question set version")
            assertThat(it.data).isEqualTo(
                mapOf(
                    "questionSetId" to created.id,
                    "version" to "-1",
                )
            )
        })
    }

    @Test
    fun testQuestionSetVersions() = runTest {
        val createRequest = QuestionSetCreateRequest("Example of questionnaire", "Example of description")
        val created = questionSetsRoutes.create(auth.accessToken, createRequest)

        assertThat(
            listOf(
                questionsRoutes.getQuestionSetVersion(created.id),
                questionsRoutes.getQuestionSetVersion(created.id, 1)
            )
        )
            .allSatisfy({
                assertThat(it.id).isEqualTo(created.id)
                assertThat(it.version).isEqualTo(1)
                assertThat(it.questions).isEmpty()
            })

        var questionsContent = """
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
        """.trimIndent().encodeToByteArray()
        var questionSetVersion = questionsRoutes.upload(
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            created.id
        )
        assertThat(
            listOf(
                questionSetVersion,
                questionsRoutes.getQuestionSetVersion(created.id),
                questionsRoutes.getQuestionSetVersion(created.id, 2),
            )
        ).allSatisfy({
            assertThat(it.id).isEqualTo(created.id)
            assertThat(it.version).isEqualTo(2)
            assertThat(it.questions)
                .hasSize(2)
                .anySatisfy({ question ->
                    assertQuestion(
                        question,
                        "1. Question1?",
                        listOf(
                            "A. 1 test 1",
                            "B. 2 test 1"
                        ),
                        listOf(0),
                        """
                       A. The right answer
                       The right answers
                    """.trimIndent()
                    )
                })
                .anySatisfy({ question ->
                    assertQuestion(
                        question,
                        "1. Question2?",
                        listOf(
                            "A. 1 test 2",
                            "B. 2 test 2"
                        ),
                        listOf(1),
                        """
                       B. The right answer
                       The right answers
                    """.trimIndent()
                    )
                })
        })



        questionsContent = """
            ```
            1. UpdatedQuestion1?

            A. 1 updated test 1
            B. 2 updated test 1

            A. Updated the right answer
            Updated the right answers
            ```
            ```
            1. UpdatedQuestion2?

            A. 1 updated test 2
            B. 2 updated test 2

            B. Updated the right answer
            Updated the right answers
            ```
        """.trimIndent().encodeToByteArray()
        questionSetVersion = questionsRoutes.upload(
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            created.id
        )
        assertThat(
            listOf(
                questionSetVersion,
                questionsRoutes.getQuestionSetVersion(created.id),
                questionsRoutes.getQuestionSetVersion(created.id, 3),
            )
        ).allSatisfy({
            assertThat(it.id).isEqualTo(created.id)
            assertThat(it.version).isEqualTo(3)
            assertThat(it.questions)
                .hasSize(2)
                .anySatisfy({ question ->
                    assertQuestion(
                        question,
                        "1. UpdatedQuestion1?",
                        listOf(
                            "A. 1 updated test 1",
                            "B. 2 updated test 1"
                        ),
                        listOf(0),
                        """
                       A. Updated the right answer
                       Updated the right answers
                    """.trimIndent()
                    )
                })
                .anySatisfy({ question ->
                    assertQuestion(
                        question,
                        "1. UpdatedQuestion2?",
                        listOf(
                            "A. 1 updated test 2",
                            "B. 2 updated test 2"
                        ),
                        listOf(1),
                        """
                       B. Updated the right answer
                       Updated the right answers
                    """.trimIndent()
                    )
                })
        })
    }

    @Test
    fun testCreateQuestionSetAndUploadQuestionsIntoIt() = runTest {
        val questionsContent = config.readResourceAsByteArray("question_sets/validQuestions1.MD")

        val randomInt = Random.nextInt()
        val createRequest =
            QuestionSetCreateRequest("Example of questionnaire $randomInt", "Example of description $randomInt")
        val created = questionSetsRoutes.create(auth.accessToken, createRequest)

        val uploadedQuestions = questionsRoutes.upload(
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent, null)),
            created.id
        ).questions
        assertValidQuestions1(uploadedQuestions)

        assertThat(questionsRoutes.getQuestionSetVersion(created.id).questions)
            .hasSize(2)
            .anySatisfy({
                assertThat(it).isEqualTo(uploadedQuestions[0])
            })
            .anySatisfy({
                assertThat(it).isEqualTo(uploadedQuestions[1])
            })
    }

    private fun assertValidQuestions1(questions: List<QuestionResponse>) {
        assertThat(questions)
            .hasSize(2)
            .anySatisfy({ question ->
                assertQuestion(
                    question,
                    """
                        1. Question?
                        
                        41: test;
                    """.trimIndent(),
                    listOf(
                        "A. 1 test1",
                        "B. 2 test 2",
                        "C. 3 test 3",
                        "D. 4 test 4",
                        "E. 5 test 5"
                    ),
                    listOf(0, 3, 4),
                    """
                       A, D, E. The right answers are 1, 4, 5.
                       The right answers are 1, 4, 5.
                    """.trimIndent()
                )
            })
            .anySatisfy({ question ->
                assertQuestion(
                    question,
                    """
                        |2. Question 2?
                        |
                        |test
                    """.trimMargin(),
                    listOf(
                        "A. a",
                        "B. aa",
                        "C. bb",
                        "D. ddd",
                        "E. eee",
                        "F. qqqq",
                    ),
                    listOf(1),
                    "B. the right answer is B - aa."
                )
            })
    }

    private fun assertQuestion(
        questionToBeAsserted: QuestionResponse,
        expectedQuestion: String,
        expectedAnswerOptions: List<String>,
        expectedCorrectAnswerIndexes: List<Int>,
        expectedCorrectAnswerExplanation: String
    ) {
        assertThat(questionToBeAsserted.id)
            .isNotBlank()
        assertThat(questionToBeAsserted.question)
            .isEqualTo(expectedQuestion)
        assertThat(questionToBeAsserted.answerOptions)
            .isEqualTo(expectedAnswerOptions)
        assertThat(questionToBeAsserted.correctAnswerIndexes)
            .isEqualTo(expectedCorrectAnswerIndexes)
        assertThat(questionToBeAsserted.correctAnswerExplanation)
            .isEqualTo(expectedCorrectAnswerExplanation)
    }

}
