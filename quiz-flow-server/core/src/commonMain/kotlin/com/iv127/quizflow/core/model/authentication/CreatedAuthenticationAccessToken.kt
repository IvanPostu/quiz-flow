package com.iv127.quizflow.core.model.authentication

data class CreatedAuthenticationAccessToken(
    val authenticationAccessToken: AuthenticationAccessToken,
    val accessToken: String,
    val authorizationScopes: Set<AuthorizationScope>
)
