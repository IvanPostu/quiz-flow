package com.iv127.quizflow.core.rest.api.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizUpdateRequest(
    @SerialName("question_set_id") val questionSetId: String,
    @SerialName("question_set_version") val questionSetVersion: Int,
    @SerialName("answers") val quizAnswerRequests: List<QuizAnswerRequest>
)

