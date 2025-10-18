package com.iv127.quizflow.core.services.authentication.impl

import com.iv127.quizflow.core.lang.Sha256
import com.iv127.quizflow.core.lang.UUIDv4
import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authentication.Authentication
import com.iv127.quizflow.core.model.authentication.AuthenticationAccessToken
import com.iv127.quizflow.core.model.authentication.AuthenticationAccessTokenNotFoundException
import com.iv127.quizflow.core.model.authentication.AuthenticationRefreshToken
import com.iv127.quizflow.core.model.authentication.AuthenticationRefreshTokenNotFoundException
import com.iv127.quizflow.core.model.authentication.AuthorizationScope
import com.iv127.quizflow.core.security.AuthenticationException
import com.iv127.quizflow.core.services.authentication.AuthenticationService
import com.iv127.quizflow.core.services.utils.DatabaseRecord
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteTimestampUtils
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AuthenticationServiceImpl(private val dbSupplier: () -> SqliteDatabase) : AuthenticationService {

    companion object {
        val REFRESH_TOKEN_TIME_TO_LIVE = 2.days
        val ACCESS_TOKEN_TIME_TO_LIVE = 10.minutes
    }

    override fun createAuthenticationRefreshToken(user: User): AuthenticationService.AuthenticationWithRefreshToken {
        val now = Clock.System.now()
        val refreshToken =
            UUIDv4.generate(UUIDv4.Companion.Format.COMPACT) + UUIDv4.generate(UUIDv4.Companion.Format.COMPACT)
        val authenticationRefreshToken = AuthenticationRefreshToken(
            id = UUIDv4.generate(),
            refreshTokenHash = Sha256.hashToHex(refreshToken.encodeToByteArray()),
            createdDate = now,
            expirationDate = now.plus(REFRESH_TOKEN_TIME_TO_LIVE),
            userId = user.id,
            authorizationScopes = getAuthorizationScopes(user)
        )

        dbSupplier().use { db ->
            val scopeIds = selectScopeIds(db, authenticationRefreshToken.authorizationScopes)

            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            db.executeAndGetChangedRowsCount(
                """
                    INSERT INTO authentication_refresh_tokens (
                        id,
                        refresh_token_hash,
                        created_at,
                        expires_at,
                        user_id) 
                    VALUES ( ?, ?, ?, ?, ? );
                """.trimIndent(),
                listOf<Any?>
                    (
                    authenticationRefreshToken.id,
                    authenticationRefreshToken.refreshTokenHash,
                    SqliteTimestampUtils.toValue(authenticationRefreshToken.createdDate),
                    SqliteTimestampUtils.toValue(authenticationRefreshToken.expirationDate),
                    authenticationRefreshToken.userId,
                )
            )
            val lastId = db.executeAndGetResultSet("SELECT last_insert_rowid() AS lastId;")[0]["lastId"]
                ?.toInt()
            for (scopeId in scopeIds) {
                db.executeAndGetChangedRowsCount(
                    """
                    INSERT INTO authentication_authorization_scope (
                        authentication_primary_key,
                        authorization_scope_primary_key) VALUES ( ?, ? );
                """.trimIndent(),
                    listOf<Any?>(lastId, scopeId)
                )
            }
            val (accessToken, authenticationAccessToken) = internalCreateAuthenticationAccessToken(
                db, authenticationRefreshToken
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            return authenticationWithRefreshToken(
                Authentication(authenticationAccessToken, accessToken, authenticationRefreshToken),
                refreshToken
            )
        }
    }

    override fun createAuthenticationAccessToken(refreshToken: String): AuthenticationService.AuthenticationWithRefreshToken {
        return authenticationWithRefreshToken(
            internalCreateAuthenticationAccessToken(refreshToken),
            refreshToken
        )
    }

    override fun checkAuthorizationScopes(accessToken: String, requiredScopes: Set<AuthorizationScope>) {
        val authentication = try {
            getAuthenticationByAccessToken(accessToken)
        } catch (e: AuthenticationAccessTokenNotFoundException) {
            throw AuthenticationException("Access token is invalid")
        }
        checkAuthorizationScopes(authentication, requiredScopes)
    }

    override fun checkAuthorizationScopes(authentication: Authentication, requiredScopes: Set<AuthorizationScope>) {
        if (requiredScopes.isEmpty()) {
            throw IllegalArgumentException("requiredScopes should not be empty")
        }
        val accessAllowed = requiredScopes.all { scope ->
            authentication.authenticationRefreshToken.authorizationScopes.contains(scope)
        }
        if (!accessAllowed) {
            throw AuthenticationException("required authorization scopes are missing")
        }
    }

    override fun getAuthenticationByAccessToken(accessToken: String): Authentication {
        val authenticationAccessToken = selectAuthenticationAccessToken(accessToken)
        val authenticationRefreshTokenByColumn = selectAuthenticationRefreshTokenByColumn(
            "id", authenticationAccessToken.id
        )
        return Authentication(authenticationAccessToken, accessToken, authenticationRefreshTokenByColumn.result)
    }

    private fun internalCreateAuthenticationAccessToken(refreshToken: String): Authentication {
        val hashedToken = Sha256.hashToHex(refreshToken.encodeToByteArray())
        val refreshTokenRecord = selectAuthenticationRefreshTokenByColumn("refresh_token_hash", hashedToken)
        val authenticationRefreshToken = refreshTokenRecord.result
        dbSupplier().use { db ->
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            val (accessToken, authenticationAccessToken) = internalCreateAuthenticationAccessToken(
                db, authenticationRefreshToken
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            return Authentication(
                authenticationAccessToken,
                accessToken,
                authenticationRefreshToken,
            )
        }
    }

    private fun internalCreateAuthenticationAccessToken(
        db: SqliteDatabase,
        authenticationRefreshToken: AuthenticationRefreshToken
    ): Pair<String, AuthenticationAccessToken> {
        val now = Clock.System.now()
        val opaqueTokenString =
            UUIDv4.generate(UUIDv4.Companion.Format.COMPACT) + UUIDv4.generate(UUIDv4.Companion.Format.COMPACT)

        val authenticationAccessToken = AuthenticationAccessToken(
            id = authenticationRefreshToken.id,
            userId = authenticationRefreshToken.userId,
            accessTokenHash = Sha256.hashToHex(opaqueTokenString.encodeToByteArray()),
            createdDate = now,
            expirationDate = now.plus(ACCESS_TOKEN_TIME_TO_LIVE)
        )
        db.executeAndGetChangedRowsCount(
            """
                    INSERT INTO authentication_access_tokens (
                        authentication_refresh_token_id,
                        user_id,
                        access_token_hash,
                        created_at,
                        expires_at ) 
                    VALUES (?, ?, ?, ?, ? );
                """.trimIndent(),
            listOf<Any?>
                (
                authenticationAccessToken.id,
                authenticationAccessToken.userId,
                authenticationAccessToken.accessTokenHash,
                SqliteTimestampUtils.toValue(authenticationAccessToken.createdDate),
                SqliteTimestampUtils.toValue(authenticationAccessToken.expirationDate),
            )
        )
        return Pair(opaqueTokenString, authenticationAccessToken)
    }

    private fun getAuthorizationScopes(user: User): Set<AuthorizationScope> {
        val isSuperAdmin = user.username == "super_admin"
        val isAdmin = user.username.startsWith("admin")
        val authorizationScopes: MutableSet<AuthorizationScope> = mutableSetOf()
        if (isSuperAdmin) {
            authorizationScopes.add(AuthorizationScope.SUPER_ADMIN)
            authorizationScopes.add(AuthorizationScope.ADMIN)
        }
        if (isAdmin) {
            authorizationScopes.add(AuthorizationScope.ADMIN)
        }
        authorizationScopes.add(AuthorizationScope.REGULAR_USER)
        return authorizationScopes.toSet()
    }

    private fun selectAuthenticationRefreshTokenByColumn(
        column: String,
        columnValue: String
    ): DatabaseRecord<AuthenticationRefreshToken> {
        dbSupplier().use { db ->
            val result = db.executeAndGetResultSet(
                """
                SELECT 
                    a.primary_key,
                    a.id,
                    a.refresh_token_hash,
                    a.created_at,
                    a.expires_at,
                    a.user_id
                FROM authentication_refresh_tokens AS a
                WHERE a.$column=?
                LIMIT 1;
            """.trimIndent(),
                listOf(columnValue)
            )
            if (result.isEmpty()) {
                throw AuthenticationRefreshTokenNotFoundException(
                    "Authentication refresh token by $column with value $columnValue was not found"
                )
            }
            val primaryKey = result[0]["primary_key"]!!.toInt()
            val authenticationRefreshToken = AuthenticationRefreshToken(
                result[0]["id"].toString(),
                result[0]["refresh_token_hash"].toString(),
                SqliteTimestampUtils.fromValue(result[0]["created_at"].toString()),
                SqliteTimestampUtils.fromValue(result[0]["expires_at"].toString()),
                result[0]["user_id"].toString(),
                selectAuthorizationScopes(db, primaryKey)
            )
            return DatabaseRecord(primaryKey, authenticationRefreshToken)
        }
    }

    private fun selectAuthenticationAccessToken(accessToken: String): AuthenticationAccessToken {
        dbSupplier().use { db ->
            val hashedAccessToken = Sha256.hashToHex(accessToken.encodeToByteArray())
            val result = db.executeAndGetResultSet(
                """
                SELECT 
                    a.primary_key,
                    a.authentication_refresh_token_id,
                    a.user_id,
                    a.access_token_hash,
                    a.created_at,
                    a.expires_at
                FROM authentication_access_tokens AS a
                WHERE a.access_token_hash=?
                LIMIT 1;
            """.trimIndent(),
                listOf(hashedAccessToken)
            )
            if (result.isEmpty()) {
                throw AuthenticationAccessTokenNotFoundException(
                    "Authentication access token by access_token_hash with value $hashedAccessToken was not found"
                )
            }
            return AuthenticationAccessToken(
                id = result[0]["authentication_refresh_token_id"].toString(),
                userId = result[0]["user_id"].toString(),
                accessTokenHash = result[0]["access_token_hash"].toString(),
                createdDate = SqliteTimestampUtils.fromValue(result[0]["created_at"].toString()),
                expirationDate = SqliteTimestampUtils.fromValue(result[0]["expires_at"].toString()),
            )
        }
    }

    private fun selectAuthorizationScopes(
        db: SqliteDatabase,
        authenticationRefreshTokenPrimaryKey: Int
    ): Set<AuthorizationScope> {
        val result = db.executeAndGetResultSet(
            """
                SELECT a.scope
                FROM authorization_scopes AS a
                WHERE a.primary_key IN (
                    SELECT authorization_scope_primary_key 
                    FROM authentication_authorization_scope
                    WHERE authentication_primary_key=?
                );
            """.trimIndent(),
            listOf(authenticationRefreshTokenPrimaryKey)
        )
        return result.map {
            AuthorizationScope.valueOf(it["scope"].toString())
        }.toSet()
    }

    private fun selectScopeIds(db: SqliteDatabase, scopes: Set<AuthorizationScope>): Set<Int> {
        val result = db.executeAndGetResultSet(
            """
                SELECT a.primary_key
                FROM authorization_scopes AS a
                WHERE a.scope IN (${scopes.joinToString(separator = ",") { "?" }});
            """.trimIndent(),
            scopes.map { it.name }
        )
        return result.map {
            it["primary_key"].toString().toInt()
        }.toSet()
    }

    private fun authenticationWithRefreshToken(
        authentication: Authentication,
        refreshToken: String
    ): AuthenticationService.AuthenticationWithRefreshToken {
        return object : AuthenticationService.AuthenticationWithRefreshToken {
            override fun authentication(): Authentication = authentication

            override fun refreshToken(): String = refreshToken

        }
    }

}
