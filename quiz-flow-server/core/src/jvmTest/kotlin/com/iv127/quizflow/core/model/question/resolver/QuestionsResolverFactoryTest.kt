package com.iv127.quizflow.core.model.question.resolver

import com.iv127.quizflow.core.model.question.Question
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat

class QuestionsResolverFactoryTest() {

    companion object {
        val MISSING_SECTION_MESSAGE = """
            {Question} - text, can contain any amount 
            of lines with text and new lines
            
            A. Answer 1
            B. Answer 2
            C. Answer 3
            <L>. Answer N - where L is an uppercased letter
            
            A, B, <L>. text, can contain any amount of lines with text without additional newlines
            Letters(question identifiers) should match question's letters
            """.trimIndent()
    }

    private val questionsResolver: QuestionsResolver;

    init {
        val factory = QuestionsResolverFactory()
        questionsResolver = factory.create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)
    }

    @Test
    fun testResolveQuestionWithOnlyTwoSections() {
        val input = """
            ```
            Which one??
            
            A. answer 1
            B. answer 1
            ```
        """.trimIndent()
        val result = questionsResolver.resolve(input).asResult()
        val exception: QuestionsResolveException = result.exceptionOrNull() as QuestionsResolveException

        assertThat(exception.reason).isEqualTo(QuestionsResolveException.Reason.REQUIRED_SECTIONS_MISSED)
        assertThat(exception.rawSource).isEqualTo(input)
        assertThat(exception.message).isEqualTo(MISSING_SECTION_MESSAGE)
    }

    @Test
    fun testResolveQuestionWithOnlyOneSection() {
        val input = """
            ```
            Which one??
            ```
        """.trimIndent()
        val result = questionsResolver.resolve(input).asResult()
        val exception: QuestionsResolveException = result.exceptionOrNull() as QuestionsResolveException

        assertThat(exception.reason).isEqualTo(QuestionsResolveException.Reason.REQUIRED_SECTIONS_MISSED)
        assertThat(exception.rawSource).isEqualTo(input)
        assertThat(exception.message).isEqualTo(MISSING_SECTION_MESSAGE)
    }


    @Test
    fun testResolveQuestionWithoutAnySections() {
        val input = """
            ```
            ```
        """.trimIndent()
        val result = questionsResolver.resolve(input).asResult()
        val exception: QuestionsResolveException = result.exceptionOrNull() as QuestionsResolveException

        assertThat(exception.reason).isEqualTo(QuestionsResolveException.Reason.REQUIRED_SECTIONS_MISSED)
        assertThat(exception.rawSource).isEqualTo(input)
        assertThat(exception.message).isEqualTo(MISSING_SECTION_MESSAGE)
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
    fun testResolveQuestionWrappedInMarkdown() {
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

    @Test
    fun testResolveQuestionWrappedInMarkdownThatDoNotHaveLettersForAnswers() {
        val factory = QuestionsResolverFactory()
        val questionsResolver = factory.create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)

        val input = """
            ```
            13. Example of question ???
            
            blah blah blah
            1. answer
            2. answer
            3. answer
            4. answer
            5. answer
            6. answer
            7. answer
            8. answer
            9. answer
            10. answer

            A, B, C. Answers explanation
            ```
        """.trimIndent()


        val result = questionsResolver.resolve(input).asResult()
        val exception: QuestionsResolveException = result.exceptionOrNull() as QuestionsResolveException

        assertThat(exception.reason).isEqualTo(QuestionsResolveException.Reason.MISSING_ANSWERS)
        assertThat(exception.rawSource).isEqualTo(input)
        assertThat(exception.message).isEqualTo("Missing answers for correct answer letters: [A, B, C]")
    }

    @Test
    fun testResolveQuestionWrappedInMarkdownWithMultilineAnswers() {
        val factory = QuestionsResolverFactory()
        val questionsResolver = factory.create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)

        val fileContent = """
            ```
            13. Example of question 
            qwe ???

            A. Abc der
            cde.
            B. Abc qwe
            x.
            C. Abc
            cde
            method.

            A, C. Answers explanation
            ```
        """.trimIndent()


        val result = questionsResolver.resolve(fileContent).asResult()
        val questions = result.getOrThrow()

        assertThat(questions)
            .hasSize(1)
            .anySatisfy({ question ->
                assertQuestion(
                    question,
                    """
                        13. Example of question 
                        qwe ???
                    """.trimIndent(),
                    listOf(
                        """
                        A. Abc der
                        cde.
                    """.trimIndent(),
                        """
                        B. Abc qwe
                        x.
                    """.trimIndent(),
                        """
                        C. Abc
                        cde
                        method.
                    """.trimIndent(),
                    ),
                    listOf(0, 2),
                    """
                       A, C. Answers explanation
                    """.trimIndent()
                )
            })
    }


    @Test
    fun testResolveQuestionWrappedInMarkdownWithNewlineAnswers() {
        val fileContent = """
            ```
            13. Example of question 
            qwe ???

            A.
            Abc der
            cde.
            B.
            Abc qwe
            x.
            C. Abc
            cde
            method.

            A, C. Answers explanation
            ```
        """.trimIndent()


        val questions = resolve(fileContent)

        assertThat(questions)
            .hasSize(1)
            .anySatisfy({ question ->
                assertQuestion(
                    question,
                    """
                        13. Example of question 
                        qwe ???
                    """.trimIndent(),
                    listOf(
                        """
                        A.
                        Abc der
                        cde.
                    """.trimIndent(),
                        """
                        B.
                        Abc qwe
                        x.
                    """.trimIndent(),
                        """
                        C. Abc
                        cde
                        method.
                    """.trimIndent(),
                    ),
                    listOf(0, 2),
                    """
                       A, C. Answers explanation
                    """.trimIndent()
                )
            })
    }

    @Test
    fun testResolveQuestionWrappedInMarkdownWithUnorderedAnswers() {
        val factory = QuestionsResolverFactory()
        val questionsResolver = factory.create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)

        val fileContent = """
            ```
            13. Example of question 
            qwe ???

            A. Abc der
            cde.
            C. Abc qwe
            x.
            B. Abc
            cde
            method.

            A, C. Answers explanation
            ```
        """.trimIndent()

        val result = questionsResolver.resolve(fileContent).asResult()
        val exception: QuestionsResolveException = result.exceptionOrNull() as QuestionsResolveException

        assertThat(exception.reason).isEqualTo(QuestionsResolveException.Reason.REQUIRES_ALPHABET_SEQUENCE_FROM_A)
        assertThat(exception.rawSource).isEqualTo(fileContent)
        assertThat(exception.message)
            .isEqualTo("Characters must start with 'A' and be a consecutive alphabetical sequence, characters: [A, C, B]")
    }

    private fun resolve(content: String): List<Question> {
        val factory = QuestionsResolverFactory()
        val questionsResolver = factory.create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)
        return try {
            questionsResolver.resolve(content)
                .asResult()
                .getOrThrow()
        } catch (e: QuestionsResolveException) {
            throw IllegalStateException(
                "Can't resolve questions, reason: ${e.reason}, message: ${e.message}, source: ${e.rawSource}"
            )
        }
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
