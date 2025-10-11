package com.iv127.quizflow.core.model.quizz

import com.iv127.quizflow.core.lang.UUIDv4
import com.iv127.quizflow.core.model.question.Question
import com.iv127.quizflow.core.model.question.QuestionSetVersion
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class QuizBuilder {

    val id: String
    val userId: String
    val questionSetId: String
    val questionSetVersion: Int
    val createdDate: Instant
    val quizQuestionsById: Map<String, Question>

    private val quizAnswers: MutableList<QuizAnswer> = mutableListOf()
    private var finalizedDate: Instant

    constructor(userId: String, questionSetVersion: QuestionSetVersion, questions: List<Question>) {
        this.id = UUIDv4.generate()
        this.userId = userId
        this.questionSetId = questionSetVersion.id
        this.questionSetVersion = questionSetVersion.version
        this.createdDate = Clock.System.now()
        this.finalizedDate = Instant.DISTANT_PAST
        this.quizQuestionsById = questions.associateBy { item -> item.id }
    }

    constructor(quiz: Quiz, questionSetVersion: QuestionSetVersion) {
        val questionsById = questionSetVersion.questions.associateBy { item -> item.id }
        this.id = quiz.id
        this.userId = quiz.userId
        this.questionSetId = quiz.questionSetId
        this.questionSetVersion = quiz.questionSetVersion
        this.createdDate = quiz.createdDate
        this.finalizedDate = quiz.finalizedDate
        this.quizQuestionsById = quiz.quizQuestionIds.associateWith { questionsById[it]!! }
    }

    fun withAnswers(answers: List<QuizAnswer>) {
        checkValidAnswers(answers)
        quizAnswers.clear()
        quizAnswers.addAll(answers)
    }

    fun withFinalized() {
        this.finalizedDate = Clock.System.now()
    }

    fun build(): Quiz {
        return Quiz(
            id = id,
            userId = userId,
            questionSetId = questionSetId,
            questionSetVersion = questionSetVersion,
            createdDate = createdDate,
            finalizedDate = finalizedDate,
            quizQuestionIds = quizQuestionsById.keys.toList(),
            quizAnswers = quizAnswers.toList()
        )
    }

    private fun checkValidAnswers(answers: List<QuizAnswer>) {
        for (answer in answers) {
            val question = quizQuestionsById[answer.questionId]
                ?: throw InvalidQuizAnswerException("Answer with id: ${answer.questionId} is not found")

            val existingAnswerIndices = question.answerOptions.indices.toSet()
            val unknownAnswerIndices = answer.chosenAnswerIndexes.filterNot { it in existingAnswerIndices }
            if (unknownAnswerIndices.isNotEmpty()) {
                throw InvalidQuizAnswerException(
                    $unknownAnswerIndices
                )
            }
        }
    }
}

