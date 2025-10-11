package com.iv127.quizflow.core.rest.api.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizUpdateRequest(
    @SerialName("finalize") val finalize: Boolean,
    @SerialName("answers") val quizAnswerRequests: List<QuizAnswerRequest>
)

