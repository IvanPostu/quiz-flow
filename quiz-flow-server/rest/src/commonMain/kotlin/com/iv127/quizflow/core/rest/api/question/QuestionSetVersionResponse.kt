package com.iv127.quizflow.core.rest.api.question

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionSetVersionResponse(
    @SerialName("id") val id: String,
    @SerialName("version") val version: Int,
    @SerialName("questions") val questions: List<QuestionResponse> = emptyList(),
)
