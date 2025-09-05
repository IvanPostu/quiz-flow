package com.iv127.quizflow.core.model.quiz.question.resolver

class QuestionsResolveException(val reason: Reason, val rawSource: String, message: String) : Exception(message) {

    enum class Reason {
        NO_QUESTIONS_FOUND,
        DUPLICATED_ANSWERS,
        REQUIRED_SECTIONS_MISSED
    }
}
