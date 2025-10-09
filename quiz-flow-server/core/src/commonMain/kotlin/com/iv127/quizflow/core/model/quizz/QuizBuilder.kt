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
    val quizQuestionsById: MutableMap<String, Question> = LinkedHashMap()
    val quizAnswers: MutableList<QuizAnswer> = mutableListOf()

    var finalizedDate: Instant = Instant.DISTANT_PAST

    constructor(userId: String, questionSetVersion: QuestionSetVersion, questions: List<Question>) {
        this.id = UUIDv4.generate()
        this.userId = userId
        this.questionSetId = questionSetVersion.id
        this.questionSetVersion = questionSetVersion.version
        this.createdDate = Clock.System.now()
        questions.forEach {
            quizQuestionsById[it.id] = it
        }
    }

    constructor(quiz: Quiz, questionSetVersion: QuestionSetVersion) {
        val questionsById: Map<String, Question> = questionSetVersion.questions.associateBy { item -> item.id }
        this.id = quiz.id
        this.userId = quiz.userId
        this.questionSetId = quiz.questionSetId
        this.questionSetVersion = quiz.questionSetVersion
        this.createdDate = quiz.createdDate
        this.finalizedDate = quiz.finalizedDate
        quiz.quizQuestionIds.forEach { quizQuestionsById[it] = questionsById[it]!! }
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
}

