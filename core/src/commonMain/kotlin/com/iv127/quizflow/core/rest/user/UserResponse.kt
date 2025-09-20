package com.iv127.quizflow.core.rest.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    @SerialName("id") val id: String,
    @SerialName("username") val username: String
)
