package com.iv127.quizflow.core.rest.api.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestionResponse(
    @SerialName("question_id") val questionId: String,
    @SerialName("question") val question: String,
    @SerialName("answer_options") val answerOptions: List<String>,
)
