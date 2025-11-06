package com.iv127.quizflow.core.services.questionset

import com.iv127.quizflow.core.model.authentication.Authentication
import com.iv127.quizflow.core.model.question.QuestionSet
import com.iv127.quizflow.core.model.question.QuestionSetBuilder
import com.iv127.quizflow.core.model.question.QuestionSetVersion
import com.iv127.quizflow.core.rest.api.SortOrder

interface QuestionSetService {

    fun createQuestionSet(
        authentication: Authentication,
        createFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit
    ): Pair<QuestionSet, QuestionSetVersion>

    fun updateQuestionSet(
        id: String,
        updateFunc: (questionSetBuilder: QuestionSetBuilder) -> Unit
    ): Pair<QuestionSet, QuestionSetVersion>

    fun archive(id: String): QuestionSet

    fun getQuestionSetWithVersionOrElseLatest(id: String, version: Int?): Pair<QuestionSet, QuestionSetVersion>

    fun getQuestionSetList(limit: Int, offset: Int, sortOrder: SortOrder): List<QuestionSet>

}
