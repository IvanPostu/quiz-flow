package com.iv127.quizflow.core.services.questionset

import com.iv127.quizflow.core.model.question.QuestionSet
import com.iv127.quizflow.core.model.question.QuestionSetBuilder
import com.iv127.quizflow.core.model.question.QuestionSetVersion
import com.iv127.quizflow.core.rest.api.SortOrder

interface QuestionSetService {

    fun createQuestionSet(
        userId: String,
        createFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit
    ): Pair<QuestionSet, QuestionSetVersion>

    fun updateQuestionSet(
        userId: String,
        id: String,
        updateFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit
    ): Pair<QuestionSet, QuestionSetVersion>

    fun archive(userId: String, id: String): QuestionSet

    fun getQuestionSetWithVersionOrElseLatest(
        userId: String,
        id: String,
        version: Int?
    ): Pair<QuestionSet, QuestionSetVersion>

    fun getQuestionSetList(
        userId: String,
        limit: Int,
        offset: Int,
        sortOrder: SortOrder
    ): List<QuestionSet>

}
