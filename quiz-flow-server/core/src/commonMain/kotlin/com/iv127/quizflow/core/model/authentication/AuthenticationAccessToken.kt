package com.iv127.quizflow.core.model.authentication

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class AuthenticationAccessToken(
    val id: String,
    val refreshTokenId: String,
    val accessTokenHash: String,
    val createdDate: Instant,
    val expirationDate: Instant
)
