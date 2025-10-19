package com.iv127.quizflow.core.rest.api.authentication

import com.iv127.quizflow.core.rest.api.InstantSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalTime::class)
@Serializable
data class AccessTokenSummaryResponse(
    @SerialName("id")
    val id: String,
    @SerialName("access_token_hash")
    val accessTokenHash: String,
    @Serializable(with = InstantSerializer::class)
    @SerialName("created_date")
    val createdDate: Instant,
    @Serializable(with = InstantSerializer::class)
    @SerialName("expiration_date")
    val expirationDate: Instant
)
