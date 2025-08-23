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
        assertEquals(questions[0].question)
        assertEquals(2, questions.size)
    }

}
