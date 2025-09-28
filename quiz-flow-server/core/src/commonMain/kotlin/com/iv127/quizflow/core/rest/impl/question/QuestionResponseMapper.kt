package com.iv127.quizflow.core.rest.impl.question

import com.iv127.quizflow.core.model.question.Question
import com.iv127.quizflow.core.rest.api.question.QuestionResponse

class QuestionResponseMapper {
    companion object {
        fun mapToResponse(question: Question): QuestionResponse =
            QuestionResponse(
                question.id,
                question.question,
                question.answerOptions,
                question.correctAnswerIndexes,
                question.correctAnswerExplanation
            )
    }
}
