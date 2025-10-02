package com.iv127.quizflow.core.rest.api.authorization

import com.iv127.quizflow.core.rest.api.InstantSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalTime::class)
@Serializable
data class AuthorizationResponse(
    @SerialName("id") val id: String,
    @SerialName("access_token") val accessToken: String,
    @SerialName("created_date") @Serializable(with = InstantSerializer::class) val createdDate: Instant,
    @SerialName("expiration_date") @Serializable(with = InstantSerializer::class) val expirationDate: Instant,
    @SerialName("user_id") val userId: String,
    @SerialName("scopes") val scopes: List<AuthorizationScopeResponse>,
)
