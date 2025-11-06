package com.iv127.quizflow.core.model.question

import com.iv127.quizflow.core.lang.UUIDv4
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class QuestionSetBuilder {
    private val id: String;
    private val _questions: MutableList<Question> = mutableListOf()
    private var latestVersion: Int = 0
    private val userId: String

    var name: String = ""
    var description: String = ""
    var createdDate = Clock.System.now()

    constructor(userId: String) {
        this.id = UUIDv4.generate()
        this.userId = userId
    }

    constructor(questionSet: QuestionSet, questionSetVersion: QuestionSetVersion) {
        this.id = questionSet.id
        this.userId = questionSet.userId
        this.name = questionSet.name
        this.description = questionSet.description
        this.latestVersion = questionSet.latestVersion
        this.createdDate = questionSet.createdDate
        this._questions.addAll(questionSetVersion.questions)
    }

    fun setQuestions(questions: List<Question>) {
        _questions.clear()
        _questions.addAll(questions)
    }

    fun buildAndIncrement(): Pair<QuestionSet, QuestionSetVersion> {
        latestVersion++
        val questionSet = QuestionSet(
            id = id,
            userId = userId,
            name = name,
            description = description,
            latestVersion = latestVersion,
            createdDate = createdDate
        )
        val questionSetVersion = QuestionSetVersion(
            id = id,
            version = latestVersion,
            questions = _questions.toList()
        )
        return Pair(questionSet, questionSetVersion)
    }
}
