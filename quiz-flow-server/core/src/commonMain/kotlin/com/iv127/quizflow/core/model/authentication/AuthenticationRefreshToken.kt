package com.iv127.quizflow.core.model.authentication

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class AuthenticationRefreshToken(
    val id: String,
    val refreshTokenHash: String,
    val createdDate: Instant,
    val expirationDate: Instant,
    val userId: String,
    val authorizationScopes: Set<AuthorizationScope>
) {

    fun isExpired(): Boolean = Clock.System.now().toEpochMilliseconds() > expirationDate.toEpochMilliseconds()

}
