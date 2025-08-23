package com.iv127.quizflow.core.model.quiz.question.resolver

import kotlin.test.Test
import kotlin.test.assertEquals

class QuestionsResolverFactoryTest {

    @Test
    fun testQuestionWrappedInMarkdownSectionHappyPath() {
        val factory = QuestionsResolverFactory()
        val questionsResolver = factory.create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)

        val resource = this.javaClass.getResource("/QuestionsResolverFactoryTest/validQuestions1.MD")
        val fileContent = resource?.readText() ?: throw IllegalArgumentException("Resource not found")


        val result = questionsResolver.resolve(fileContent).asResult()
        val questions = result.getOrThrow()

        assertEquals(2, questions.size)
        assertEquals(
            questions[0].question, """1. Question?
41: test;
"""
        )
        assertEquals(
            listOf(
                "A. 1 test1",
                "B. 2 test 2",
                "C. 3 test 3 ",
                "D. 4 test 4",
                "E. 5 test 5"
            ), questions[0].answerOptions
        )
        assertEquals(listOf(0, 3, 4), questions[0].correctAnswerIndexes)
        assertEquals(
            """A, D, E. The right answers are 1, 4, 5.
The right answers are 1, 4, 5.""", questions[0].correctAnswerExplanation!!
        )

        assertEquals(
            questions[1].question, """2. Question 2?
test
"""
        )
        assertEquals(
            listOf(
                "A. a",
                "B. aa",
                "C. bb",
                "D. ddd",
                "E. eee",
                "F. qqqq",
            ), questions[1].answerOptions
        )
        assertEquals(listOf(1), questions[1].correctAnswerIndexes)
        assertEquals(
            """B. the right answer is B - aa.""", questions[1].correctAnswerExplanation!!
        )

    }

}
