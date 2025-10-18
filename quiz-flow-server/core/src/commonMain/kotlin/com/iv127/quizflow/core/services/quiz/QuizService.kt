package com.iv127.quizflow.core.services.quiz

import com.iv127.quizflow.core.model.question.Question
import com.iv127.quizflow.core.model.question.QuestionSetVersion
import com.iv127.quizflow.core.model.quizz.Quiz
import com.iv127.quizflow.core.model.quizz.QuizBuilder
import com.iv127.quizflow.core.rest.api.SortOrder

interface QuizService {

    fun getQuizList(
        userId: String,
        offset: Int,
        limit: Int,
        sortOrder: SortOrder,
        finalizedOnly: Boolean = false
    ): List<Quiz>

    fun getQuiz(
        userId: String,
        quizId: String,
    ): Quiz

    fun createQuiz(
        userId: String,
        questionSetVersion: QuestionSetVersion,
        questions: List<Question>
    ): Quiz

    fun updateQuiz(
        userId: String,
        quizId: String,
        questionSetVersion: QuestionSetVersion,
        updateFunc: (quizBuilder: QuizBuilder) -> Unit
    ): Quiz

}
