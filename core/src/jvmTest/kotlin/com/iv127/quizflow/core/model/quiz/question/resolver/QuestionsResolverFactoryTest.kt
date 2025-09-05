package com.iv127.quizflow.core.model.quiz.question.resolver

import com.iv127.quizflow.core.model.quiz.question.Question
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat

class QuestionsResolverFactoryTest() {

    private val questionsResolver: QuestionsResolver;

    init {
        val factory = QuestionsResolverFactory()
        questionsResolver = factory.create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)
    }

    @Test
    fun testResolveQuestionsForNoisyStringInput() {
        val noisyString = (0..255).map { it.toByte() }.toByteArray().fold("") { acc, byte ->
            acc + byte.toInt().toChar()
        }
        val result = questionsResolver.resolve(noisyString).asResult()
        val exception: QuestionsResolveException = result.exceptionOrNull() as QuestionsResolveException

        assertThat(exception.reason).isEqualTo(QuestionsResolveException.Reason.NO_QUESTIONS_FOUND)
        assertThat(exception.rawSource).isEqualTo(noisyString)
        assertThat(exception.message).isEqualTo("Can't find questions from specified source")
    }

    @Test
    fun testResolveQuestionsForEmptyInput() {
        val result = questionsResolver.resolve("").asResult()
        val exception: QuestionsResolveException = result.exceptionOrNull() as QuestionsResolveException

        assertThat(exception.reason).isEqualTo(QuestionsResolveException.Reason.NO_QUESTIONS_FOUND)
        assertThat(exception.rawSource).isEqualTo("")
        assertThat(exception.message).isEqualTo("Can't find questions from specified source")
    }

    @Test
    fun testQuestionWrappedInMarkdownSectionHappyPath() {
        val factory = QuestionsResolverFactory()
        val questionsResolver = factory.create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)

        val resource = this.javaClass.getResource("/QuestionsResolverFactoryTest/validQuestions1.MD")
        val fileContent = resource?.readText() ?: throw IllegalArgumentException("Resource not found")


        val result = questionsResolver.resolve(fileContent).asResult()
        val questions = result.getOrThrow()

        assertThat(questions)
            .hasSize(2)
            .anySatisfy({ question ->
                assertQuestion(
                    question,
                    """
                        |1. Question?
                        |
                        |41: test;
                    """.trimMargin(),
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
        questionToBeAsserted: Question,
        expectedQuestion: String,
        expectedAnswerOptions: List<String>,
        expectedCorrectAnswerIndexes: List<Int>,
        expectedCorrectAnswerExplanation: String
    ) {

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
