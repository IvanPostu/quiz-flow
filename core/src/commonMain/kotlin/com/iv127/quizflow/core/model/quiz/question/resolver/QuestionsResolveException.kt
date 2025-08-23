package com.iv127.quizflow.core.model.quiz.question.resolver

class QuestionsResolveException(val reason: Reason, val rawSource: String, override val message: String) :
    Exception(message) {

    enum class Reason {
        NO_QUESTIONS_FOUND,
        INVALID_FORMAT,
        DUPLICATED_ANSWERS,
        REQUIRED_SECTIONS_MISSED
    }
}
