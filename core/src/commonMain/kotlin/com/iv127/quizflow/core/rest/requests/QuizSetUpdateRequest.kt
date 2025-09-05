package com.iv127.quizflow.core.rest.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizSetUpdateRequest(
    @SerialName("name") private val name: String,
    @SerialName("description") private val description: String
)
