package com.iv127.quizflow.core.rest.api.authentication

import com.iv127.quizflow.core.rest.api.InstantSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalTime::class)
@Serializable
data class RefreshTokenSummaryResponse(
    @SerialName("id")
    val id: String,
    @SerialName("refresh_token_hash")
    val refreshTokenHash: String,
    @SerialName("created_date")
    @Serializable(with = InstantSerializer::class)
    val createdDate: Instant,
    @SerialName("expiration_date")
    @Serializable(with = InstantSerializer::class)
    val expirationDate: Instant,
    @SerialName("authorization_scopes")
    val authorizationScopes: Set<AuthorizationScopeResponse>,
    @SerialName("access_tokens")
    val accessTokens: List<AccessTokenSummaryResponse>,
)
