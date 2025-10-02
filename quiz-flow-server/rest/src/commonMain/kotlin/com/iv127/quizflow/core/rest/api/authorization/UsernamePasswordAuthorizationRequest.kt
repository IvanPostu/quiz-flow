package com.iv127.quizflow.core.rest.api.authorization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsernamePasswordAuthorizationRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
)

