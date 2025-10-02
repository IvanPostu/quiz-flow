package com.iv127.quizflow.core.model.authorization

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Authorization(
    val id: String,
    val accessToken: String,
    val createdDate: Instant,
    val expirationDate: Instant,
    val userId: String,
    val impersonateOriginAuthorization: Authorization?,
    val authorizationScopes: List<AuthorizationScope>
) {
    fun isExpired(): Boolean = Clock.System.now().toEpochMilliseconds() > expirationDate.toEpochMilliseconds()
}
