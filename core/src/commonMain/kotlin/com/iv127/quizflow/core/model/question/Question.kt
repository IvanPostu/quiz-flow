package com.iv127.quizflow.core.model.question

data class Question(
    val question: String,
    val answerOptions: List<String>,
    val correctAnswerIndexes: List<Int>,
    val correctAnswerExplanation: String
)
