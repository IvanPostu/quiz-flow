package com.iv127.quizflow.core.rest.api.questionset

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionSetCreateRequest(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String
)
