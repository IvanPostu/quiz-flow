package com.iv127.quizflow.core.model.quiz.question.resolver

import kotlin.test.Test
import kotlin.test.assertEquals
import org.assertj.core.api.Assertions.assertThat

class QuestionsResolverFactoryTest {

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
                assertThat(question.question)
                    .isEqualTo(
                        """
                            |1. Question?
                            |
                            |41: test;
                        """.trimMargin()
                    )
                assertEquals(
                    listOf(
                        "A. 1 test1",
                        "B. 2 test 2",
                        "C. 3 test 3 ",
                        "D. 4 test 4",
                        "E. 5 test 5"
                    ), question.answerOptions
                )
                assertEquals(listOf(0, 3, 4), question.correctAnswerIndexes)
                assertEquals(
                    """
                       A, D, E. The right answers are 1, 4, 5.
                       The right answers are 1, 4, 5.
                    """.trimIndent(),
                    question.correctAnswerExplanation
                )
            })
            .anySatisfy({ question ->
                assertEquals(
                    """
                        |2. Question 2?
                        |
                        |test
                    """.trimMargin(),
                    question.question
                )
                assertEquals(
                    listOf(
                        "A. a",
                        "B. aa",
                        "C. bb",
                        "D. ddd",
                        "E. eee",
                        "F. qqqq",
                    ), question.answerOptions
                )
                assertEquals(listOf(1), question.correctAnswerIndexes)
                assertEquals(
                    "B. the right answer is B - aa.", question.correctAnswerExplanation
                )
            })
    }

}
