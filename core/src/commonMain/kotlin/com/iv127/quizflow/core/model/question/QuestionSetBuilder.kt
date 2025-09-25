package com.iv127.quizflow.core.model.question

import com.iv127.quizflow.core.lang.UUIDv4

class QuestionSetBuilder {
    private val id: String;
    private val _questions: MutableList<Question> = mutableListOf()
    private var version: Int = 0
    var name: String = ""
    var description: String = ""

    constructor() {
        this.id = UUIDv4.generate()
    }

    constructor(questionSet: QuestionSet) {
        this.id = questionSet.id
        this.name = questionSet.name
        this.description = questionSet.description
        this.version = questionSet.version
        this._questions.addAll(questionSet.questions)
    }

    fun setQuestions(questions: List<Question>) {
        _questions.clear()
        _questions.addAll(questions)
    }

    fun build(): QuestionSet {
        return QuestionSet(
            id = id,
            name = name,
            description = description,
            version = ++version,
            questions = _questions.toList()
        )
    }
}
