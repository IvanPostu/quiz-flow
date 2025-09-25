package com.iv127.quizflow.core.services

import com.iv127.quizflow.core.model.question.QuestionSet
import com.iv127.quizflow.core.model.question.QuestionSetBuilder

interface QuestionSetService {

    fun createQuestionSet(createFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit): QuestionSet

    fun updateQuestionSet(id: String, updateFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit): QuestionSet

    fun archive(id: String): QuestionSet

    fun getQuestionSet(id: String): QuestionSet

    fun getQuestionSet(): List<QuestionSet>

}
