package com.iv127.quizflow.core.model.authorization

import com.iv127.quizflow.core.lang.UUIDv4
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class AuthorizationBuilder {
    val id: String
    val accessToken: String
    val createdDate: Instant
    var expirationDate: Instant
    var userId: String = ""
    var impersonateOriginAuthorization: Authorization? = null
    var authorizationScopes: Set<AuthorizationScope>

    constructor() {
        id = UUIDv4.generate()
        accessToken =
            UUIDv4.generate(UUIDv4.Companion.Format.COMPACT) + UUIDv4.generate(UUIDv4.Companion.Format.COMPACT)
        createdDate = Clock.System.now()
        expirationDate = createdDate
        userId = ""
        impersonateOriginAuthorization = null
        authorizationScopes = setOf(AuthorizationScope.REGULAR_USER)
    }

    constructor(authorization: Authorization) {
        id = authorization.id
        accessToken = authorization.accessToken
        createdDate = authorization.createdDate
        expirationDate = authorization.expirationDate
        userId = authorization.userId
        impersonateOriginAuthorization = authorization.impersonateOriginAuthorization
        authorizationScopes = authorization.authorizationScopes
    }

    fun setExpirationDate(expirationDateSetter: (createdDate: Instant) -> Instant) = apply {
        this.expirationDate = expirationDateSetter(this.createdDate)
    }

    fun setUserId(userId: String) = apply { this.userId = userId }
    fun setImpersonateOriginAuthorization(impersonateOriginAuthorization: Authorization) = apply {
        this.impersonateOriginAuthorization = impersonateOriginAuthorization
    }

    fun setAuthorizationScopes(authorizationScopes: Set<AuthorizationScope>) = apply {
        this.authorizationScopes = authorizationScopes.toSet()
    }

    fun build(): Authorization {
        return Authorization(
            id = id,
            accessToken = accessToken,
            createdDate = createdDate,
            expirationDate = expirationDate,
            userId = userId,
            impersonateOriginAuthorization = impersonateOriginAuthorization,
            authorizationScopes = authorizationScopes
        )
    }
}
