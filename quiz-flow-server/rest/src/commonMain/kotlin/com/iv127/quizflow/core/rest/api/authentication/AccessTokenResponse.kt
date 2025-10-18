package com.iv127.quizflow.core.rest.api.authentication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("authorization_scopes") val authorizationScopes: Set<AuthorizationScopeResponse>
)
