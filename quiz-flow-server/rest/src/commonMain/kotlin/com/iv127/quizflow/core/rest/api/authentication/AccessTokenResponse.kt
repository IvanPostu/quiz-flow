package com.iv127.quizflow.core.rest.api.authentication

import com.iv127.quizflow.core.rest.api.InstantSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalTime::class)
@Serializable
data class AccessTokenResponse(
    @SerialName("access_token_id")
    val accessTokenId: String,
    @SerialName("refresh_token_id")
    val refreshTokenId: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("authorization_scopes")
    val authorizationScopes: Set<AuthorizationScopeResponse>,
    @SerialName("access_token_expiration_date")
    @Serializable(with = InstantSerializer::class)
    val accessTokenExpirationDate: Instant,
    @SerialName("refresh_token_expiration_date")
    @Serializable(with = InstantSerializer::class)
    val refreshTokenExpirationDate: Instant,
)
