package com.iv127.quizflow.core.rest.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionsSetCreateRequest(
    @SerialName("name") val name: String,
    @SerialName("description") val description: String
)
