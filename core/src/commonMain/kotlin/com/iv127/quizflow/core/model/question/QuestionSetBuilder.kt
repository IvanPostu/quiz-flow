package com.iv127.quizflow.core.model.question

import com.iv127.quizflow.core.lang.UUIDv4

class QuestionSetBuilder {
    private val id: String;
    private val _questions: MutableList<Question> = mutableListOf()
    private var latestVersion: Int = 0
    var name: String = ""
    var description: String = ""

    constructor() {
        this.id = UUIDv4.generate()
    }

    constructor(questionSet: QuestionSet, questionSetVersion: QuestionSetVersion) {
        this.id = questionSet.id
        this.name = questionSet.name
        this.description = questionSet.description
        this.latestVersion = questionSet.latestVersion
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
            name = name,
            description = description,
            latestVersion = latestVersion
        )
        val questionSetVersion = QuestionSetVersion(
            id = id,
            version = latestVersion,
            questions = _questions.toList()
        )
        return Pair(questionSet, questionSetVersion)
    }
}
