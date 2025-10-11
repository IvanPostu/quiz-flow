package com.iv127.quizflow.core.services.quiz

import com.iv127.quizflow.core.model.authorization.Authorization
import com.iv127.quizflow.core.model.question.Question
import com.iv127.quizflow.core.model.question.QuestionSetVersion
import com.iv127.quizflow.core.model.quizz.Quiz
import com.iv127.quizflow.core.model.quizz.QuizBuilder

interface QuizService {

    fun getQuiz(
        authorization: Authorization,
        quizId: String,
    ): Quiz

    fun createQuiz(
        authorization: Authorization,
        questionSetVersion: QuestionSetVersion,
        questions: List<Question>
    ): Quiz

    fun updateQuiz(
        authorization: Authorization,
        quizId: String,
        questionSetVersion: QuestionSetVersion,
        updateFunc: (quizBuilder: QuizBuilder) -> Unit
    ): Quiz

}
