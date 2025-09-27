package com.iv127.quizflow.core.rest.api.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserCreateRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)
