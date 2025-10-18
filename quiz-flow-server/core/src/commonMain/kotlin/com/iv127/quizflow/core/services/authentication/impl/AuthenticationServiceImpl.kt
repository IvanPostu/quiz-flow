package com.iv127.quizflow.core.services.authentication.impl

import com.iv127.quizflow.core.lang.Sha256
import com.iv127.quizflow.core.lang.UUIDv4
import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authentication.Authentication
import com.iv127.quizflow.core.model.authentication.AuthenticationAccessToken
import com.iv127.quizflow.core.model.authentication.AuthenticationNotFoundException
import com.iv127.quizflow.core.model.authentication.AuthenticationRefreshToken
import com.iv127.quizflow.core.model.authentication.AuthorizationScope
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

    override fun createAuthenticationRefreshToken(user: User): Authentication {
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
            return Authentication(authenticationAccessToken, accessToken, authenticationRefreshToken, refreshToken)
        }
    }

    override fun createAuthenticationAccessToken(refreshToken: String): Authentication {
        return internalCreateAuthenticationAccessToken(refreshToken)
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
                refreshToken
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
                throw AuthenticationNotFoundException(
                    "Authentication by $column with value $columnValue was not found"
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
                WHERE a.scope IN (${scopes.map { "?" }.joinToString(separator = ",")});
            """.trimIndent(),
            scopes.map { it.name }
        )
        return result.map {
            it["primary_key"].toString().toInt()
        }.toSet()
    }
}
