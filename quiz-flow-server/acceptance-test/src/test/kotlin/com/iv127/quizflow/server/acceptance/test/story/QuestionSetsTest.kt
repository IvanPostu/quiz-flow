package com.iv127.quizflow.server.acceptance.test.story

import com.iv127.quizflow.core.rest.api.MultipartData
import com.iv127.quizflow.core.rest.api.question.QuestionResponse
import com.iv127.quizflow.core.rest.api.question.QuestionsRoutes
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionSetsRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionsRoutesTestImpl
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QuestionSetsTest {

    private val config: GlobalConfig = GlobalConfig.INSTANCE
    private val questionSetsRoutes: QuestionSetsRoutes = QuestionSetsRoutesTestImpl()
    private val questionsRoutes: QuestionsRoutes = QuestionsRoutesTestImpl()

    @Test
    fun testCreateQuestionSetUploadQuestionsIntoIt() = runTest {
        val createRequest = QuestionSetCreateRequest("Example of questionnaire", "Example of description")
        val created = questionSetsRoutes.create(createRequest)

        val questionsContent = config.readResourceAsByteArray("question_sets/validQuestions1.MD")
        val uploadedQuestions = questionsRoutes.upload(
            listOf(MultipartData.FilePart("file", "questions.MD", questionsContent)),
            created.id
        )
        assertValidQuestions1(uploadedQuestions)

        assertThat(questionsRoutes.get(created.id, uploadedQuestions[0].id))
            .isEqualTo(uploadedQuestions[0])
        assertThat(questionsRoutes.get(created.id, uploadedQuestions[1].id))
            .isEqualTo(uploadedQuestions[1])
        assertThat(questionsRoutes.list(created.id))
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
                        "C. 3 test 3 ",
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
