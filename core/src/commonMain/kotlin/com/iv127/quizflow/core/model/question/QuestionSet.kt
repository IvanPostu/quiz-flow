package com.iv127.quizflow.core.model.question

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionSet(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("version") val version: Int,
    @SerialName("questions") val questions: List<Question> = emptyList(),
)

