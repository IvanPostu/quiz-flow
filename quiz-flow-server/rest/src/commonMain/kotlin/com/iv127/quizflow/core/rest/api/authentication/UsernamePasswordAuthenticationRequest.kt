package com.iv127.quizflow.core.rest.api.authentication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsernamePasswordAuthenticationRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String,
)

