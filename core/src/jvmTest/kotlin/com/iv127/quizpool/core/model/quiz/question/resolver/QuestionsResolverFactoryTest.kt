package com.iv127.quizpool.core.model.quiz.question.resolver

import kotlin.test.Test
import kotlin.test.assertEquals

class QuestionsResolverFactoryTest {

    @Test
    fun testQuestionWrappedInMarkdownSection() {
        val factory = QuestionsResolverFactory()
        val questionsResolver = factory.create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)

        val resource = this.javaClass.getResource("/QuestionsResolverFactoryTest/validQuestions1.MD")
        val fileContent = resource?.readText() ?: throw IllegalArgumentException("Resource not found")


        var questions = questionsResolver.resolve(fileContent)

        assertEquals(1, 12)
    }

}
