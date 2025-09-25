package com.iv127.quizflow.core.rest.question

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionResponse(
    @SerialName("id") val id: String,
    @SerialName("question") val question: String,
    @SerialName("answer_options") val answerOptions: List<String>,
    @SerialName("correct_answer_indexes") val correctAnswerIndexes: List<Int>,
    @SerialName("correct_answer_explanation") val correctAnswerExplanation: String
)
