package com.iv127.quizflow.core.services.authorization.impl

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authorization.Authorization
import com.iv127.quizflow.core.model.authorization.AuthorizationBuilder
import com.iv127.quizflow.core.model.authorization.AuthorizationNotFoundException
import com.iv127.quizflow.core.model.authorization.AuthorizationScope
import com.iv127.quizflow.core.services.authorization.AuthorizationService
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.SqliteTimestampUtils
import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.util.logging.KtorSimpleLogger
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AuthorizationServiceImpl(private val dbSupplier: () -> SqliteDatabase) : AuthorizationService {
    companion object {
        private val LOG = KtorSimpleLogger(getClassFullName(AuthorizationServiceImpl::class))
        val TOKEN_TIME_TO_LIVE = 2.days
    }

    override fun create(user: User, originAuthorization: Authorization?): Authorization {
        val createdAuthorization = AuthorizationBuilder().apply {
            setExpirationDate { createdDate ->
                createdDate.plus(TOKEN_TIME_TO_LIVE)
            }
            setAuthorizationScopes(getAuthorizationScopes(user))
            setUserId(user.id)
        }.build()
        dbSupplier().use { db ->
            val scopeIds = selectScopeIds(db, createdAuthorization.authorizationScopes)

            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            db.executeAndGetChangedRowsCount(
                """
                    INSERT INTO authorizations (
                        id,
                        access_token,
                        created_at,
                        expires_at,
                        user_id,
                        impersonate_origin_authorization_id) VALUES (
                        ?, ?, ?, ?, ?, ?);
                """.trimIndent(),
                listOf<Any?>
                    (
                    createdAuthorization.id,
                    createdAuthorization.accessToken,
                    SqliteTimestampUtils.toValue(createdAuthorization.createdDate),
                    SqliteTimestampUtils.toValue(createdAuthorization.expirationDate),
                    createdAuthorization.userId,
                    createdAuthorization.impersonateOriginAuthorization?.id
                )
            )
            val lastId = db.executeAndGetResultSet("SELECT last_insert_rowid() AS lastId;")[0]["lastId"]
                ?.toInt()
            for (scopeId in scopeIds) {
                db.executeAndGetChangedRowsCount(
                    """
                    INSERT INTO authorization_authorization_scope (
                        authorization_primary_key,
                        authorization_scope_primary_key) VALUES ( ?, ? );
                """.trimIndent(),
                    listOf<Any?>(lastId, scopeId)
                )
            }
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            return createdAuthorization
        }
    }

    override fun updateById(
        authorization: Authorization,
        authorizationId: String,
        updateFunc: (authorizationBuilder: AuthorizationBuilder) -> Unit
    ): Authorization {
        val authorizationToUpdate = selectAuthorizationByColumn("id", authorizationId, false)
        val updatedAuthorization = AuthorizationBuilder(authorization).apply {
            updateFunc(this)
        }.build()

        dbSupplier().use { db ->
            val oldScopeIds = selectScopeIds(db, authorizationToUpdate.authorizationScopes)
            val newScopeIds = selectScopeIds(db, updatedAuthorization.authorizationScopes)

            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            db.executeAndGetChangedRowsCount(
                """
                    UPDATE authorizations SET 
                        access_token=?, created_at=?, expires_at=?, user_id=?, 
                        impersonate_origin_authorization_id=? WHERE id=?;
                """.trimIndent(),
                listOf<Any?>
                    (
                    updatedAuthorization.accessToken,
                    SqliteTimestampUtils.toValue(updatedAuthorization.createdDate),
                    SqliteTimestampUtils.toValue(updatedAuthorization.expirationDate),
                    updatedAuthorization.userId,
                    updatedAuthorization.impersonateOriginAuthorization?.id
                )
            )
            val lastId = db.executeAndGetResultSet("SELECT last_insert_rowid() AS lastId;")[0]["lastId"]
                ?.toInt()
            for (scopeId in oldScopeIds) {
                db.executeAndGetChangedRowsCount(
                    """
                    DELETE FROM authorization_authorization_scope WHERE
                        authorization_primary_key=? AND authorization_scope_primary_key=?;
                """.trimIndent(),
                    listOf<Any?>(lastId, scopeId)
                )
            }
            for (scopeId in newScopeIds) {
                db.executeAndGetChangedRowsCount(
                    """
                    INSERT INTO authorization_authorization_scope (
                        authorization_primary_key,
                        authorization_scope_primary_key) VALUES ( ?, ? );
                """.trimIndent(),
                    listOf<Any?>(lastId, scopeId)
                )
            }
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
        }
        return updatedAuthorization
    }

    override fun getByAccessToken(accessToken: String): Authorization? {
        try {
            val authorization = selectAuthorizationByColumn("access_token", accessToken, false)
            return authorization
        } catch (e: Exception) {
            LOG.warn("Can't get authorization by access_token due to: ${e.message}")
            return null
        }
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

    private fun selectAuthorizationByColumn(
        column: String,
        columnValue: String,
        isOrigin: Boolean = false
    ): Authorization {
        dbSupplier().use { db ->
            val result = db.executeAndGetResultSet(
                """
                SELECT 
                    a.primary_key,
                    a.id,
                    a.access_token,
                    a.created_at,
                    a.expires_at,
                    a.user_id,
                    a.impersonate_origin_authorization_id
                FROM authorizations AS a
                WHERE a.$column=?
            """.trimIndent(),
                listOf(columnValue)
            )
            if (result.isEmpty()) {
                throw AuthorizationNotFoundException(
                    "Authorization by column $column with value $columnValue was not found"
                )
            }
            val originId = result[0]["impersonate_origin_authorization_id"]
            val impersonateOriginAuthorization: Authorization? = if (isOrigin || originId == null) null else
                selectAuthorizationByColumn("id", originId, true)

            return Authorization(
                result[0]["id"].toString(),
                result[0]["access_token"].toString(),
                SqliteTimestampUtils.fromValue(result[0]["created_at"].toString()),
                SqliteTimestampUtils.fromValue(result[0]["expires_at"].toString()),
                result[0]["user_id"].toString(),
                impersonateOriginAuthorization,
                selectAuthorizationScopes(db, result[0]["id"].toString())
            )
        }
    }

    private fun selectAuthorizationScopes(db: SqliteDatabase, authorizationId: String): Set<AuthorizationScope> {
        val result = db.executeAndGetResultSet(
            """
                SELECT a.scope
                FROM authorization_scopes AS a
                WHERE a.primary_key IN (
                    SELECT authorization_scope_primary_key 
                    FROM authorization_authorization_scope
                    WHERE authorization_primary_key=?
                );
            """.trimIndent(),
            listOf(authorizationId)
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
