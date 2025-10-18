package com.iv127.quizflow.core.model.authentication

data class CreatedAuthenticationRefreshToken(
    val authenticationRefreshToken: AuthenticationRefreshToken,
    val plainRefreshToken: String
)
