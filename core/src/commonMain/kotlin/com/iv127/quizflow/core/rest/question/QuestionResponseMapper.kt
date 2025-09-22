package com.iv127.quizflow.core.rest.question

import com.iv127.quizflow.core.model.question.Question

class QuestionResponseMapper {
    companion object {
        fun mapToResponse(question: Question): QuestionResponse =
            QuestionResponse(
                question.question,
                question.answerOptions,
                question.correctAnswerIndexes,
                question.correctAnswerExplanation
            )
    }
}
