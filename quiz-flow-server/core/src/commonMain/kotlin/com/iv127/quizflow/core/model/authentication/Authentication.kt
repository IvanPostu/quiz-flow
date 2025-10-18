package com.iv127.quizflow.core.model.authentication

data class Authentication(
    val authenticationAccessToken: AuthenticationAccessToken,
    val accessToken: String,
    val authenticationRefreshToken: AuthenticationRefreshToken
)
