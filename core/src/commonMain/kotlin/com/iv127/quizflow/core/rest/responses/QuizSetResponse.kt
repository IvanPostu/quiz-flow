package com.iv127.quizflow.core.rest.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizSetResponse(
    @SerialName("id") private val id: String,
    @SerialName("name") private val name: String,
    @SerialName("description") private val description: String
)
