package com.iv127.quizflow.core.model.question

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionSetVersion(
    @SerialName("id") val id: String,
    @SerialName("version") val version: Int,
    @SerialName("questions") val questions: List<Question> = emptyList(),
)
