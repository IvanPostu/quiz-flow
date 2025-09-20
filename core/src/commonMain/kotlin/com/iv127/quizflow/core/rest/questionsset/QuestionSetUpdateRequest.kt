package com.iv127.quizflow.core.rest.questionsset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionSetUpdateRequest(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String
)
