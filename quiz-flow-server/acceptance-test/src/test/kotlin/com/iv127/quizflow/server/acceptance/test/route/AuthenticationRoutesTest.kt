package com.iv127.quizflow.server.acceptance.test.route

import com.iv127.quizflow.core.rest.api.authentication.AuthorizationScopeResponse
import com.iv127.quizflow.core.rest.api.authentication.MarkAccessTokenAsExpiredRequest
import com.iv127.quizflow.core.rest.api.authentication.MarkRefreshTokenAsExpiredRequest
import com.iv127.quizflow.core.rest.api.authentication.UsernamePasswordAuthenticationRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieRequest
import com.iv127.quizflow.server.acceptance.test.acceptance.AuthenticationAcceptance
import com.iv127.quizflow.server.acceptance.test.acceptance.UserAcceptance
import com.iv127.quizflow.server.acceptance.test.rest.RestErrorException
import com.iv127.quizflow.server.acceptance.test.rest.impl.AuthenticationsRoutesTestImpl
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@OptIn(ExperimentalTime::class)
class AuthenticationRoutesTest {

    private val authenticationsRoutes = AuthenticationsRoutesTestImpl()

    @Test
    fun testMarkRefreshTokenExpired() = runTest {
        val superUserAuth = AuthenticationAcceptance.authenticateSuperUser()

        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        UserAcceptance.createUser(username, password)
        val (cookies, response) = authenticationsRoutes.signIn(
            UsernamePasswordAuthenticationRequest(
                username,
                password
            )
        )
        assertThat(cookies).hasSize(1)

        val expiredRefreshTokenResponse = authenticationsRoutes
            .markRefreshTokenAsExpired(
                superUserAuth.accessToken,
                MarkRefreshTokenAsExpiredRequest(response.refreshTokenId)
            )
        assertThat(expiredRefreshTokenResponse.expirationDate)
            .isEqualTo(expiredRefreshTokenResponse.createdDate)
        assertThat(expiredRefreshTokenResponse.createdDate)
            .isLessThan(response.refreshTokenExpirationDate)
    }

    @Test
    fun testMarkAccessTokenExpired() = runTest {
        val superUserAuth = AuthenticationAcceptance.authenticateSuperUser()

        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        UserAcceptance.createUser(username, password)
        val (cookies, response) = authenticationsRoutes.signIn(
            UsernamePasswordAuthenticationRequest(
                username,
                password
            )
        )
        assertThat(cookies).hasSize(1)

        val expiredAccessTokenResponse = authenticationsRoutes
            .markAccessTokenAsExpired(
                superUserAuth.accessToken,
                MarkAccessTokenAsExpiredRequest(response.accessTokenId)
            )
        assertThat(expiredAccessTokenResponse.expirationDate)
            .isEqualTo(expiredAccessTokenResponse.createdDate)
        assertThat(expiredAccessTokenResponse.createdDate)
            .isLessThan(response.accessTokenExpirationDate)
    }

    @Test
    fun testExtendAccessTokenLifetime() = runTest {
        val delayMillis = 1000L
        val signInResult = authenticationsRoutes.signIn(
            UsernamePasswordAuthenticationRequest(
                "super_admin",
                "super_admin"
            )
        )
        val accessTokenAuthentication = signInResult.second
        assertThat(accessTokenAuthentication.accessToken).hasSize(64)

        realDelayFunction(delayMillis)

        val updatedAccessToken = authenticationsRoutes
            .extendAccessTokenLifetime(accessTokenAuthentication.accessToken)


        assertThat(updatedAccessToken.accessToken)
            .isEqualTo(accessTokenAuthentication.accessToken)
        assertThat(updatedAccessToken.authorizationScopes)
            .isEqualTo(accessTokenAuthentication.authorizationScopes)
        assertThat(updatedAccessToken.refreshTokenExpirationDate)
            .isEqualTo(accessTokenAuthentication.refreshTokenExpirationDate)
        assertThat(updatedAccessToken.accessTokenExpirationDate.toEpochMilliseconds())
            .isGreaterThanOrEqualTo(
                accessTokenAuthentication.accessTokenExpirationDate.toEpochMilliseconds() + delayMillis
            )
    }

    @Test
    fun testExtendLifetimeOfInvalidAccessToken() = runTest {

        val e = assertThrows<RestErrorException> {
            authenticationsRoutes.extendAccessTokenLifetime(
                "whatever"
            )
        }
        assertAuthenticationFailed(e, "Access token is invalid")
    }

    @Test
    fun testExtendLifetimeOfExpiredAccessToken() = runTest {
        val superUserAuthentication = AuthenticationAcceptance.authenticateSuperUser()

        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        UserAcceptance.createUser(username, password)
        val (_, response) = authenticationsRoutes.signIn(
            UsernamePasswordAuthenticationRequest(
                username,
                password
            )
        )

        authenticationsRoutes.markAccessTokenAsExpired(
            superUserAuthentication.accessToken,
            MarkAccessTokenAsExpiredRequest(response.accessTokenId)
        )

        val e = assertThrows<RestErrorException> {
            authenticationsRoutes.extendAccessTokenLifetime(response.accessToken)
        }
        assertAuthenticationFailed(e, "Access token is expired")
    }

    @Test
    fun testCreateAccessTokenWithoutRefreshableToken() = runTest {

        val e = assertThrows<RestErrorException> {
            authenticationsRoutes.createAccessToken(
                listOf()
            )
        }
        assertAuthenticationFailed(e, "Refresh token is not found in the cookie")
    }

    @Test
    fun testCreateAccessTokenWithInvalidRefreshableToken() = runTest {

        val e = assertThrows<RestErrorException> {
            authenticationsRoutes.createAccessToken(
                listOf(
                    CookieRequest("refreshable_token", "whatever")
                )
            )
        }
        assertAuthenticationFailed(e, "Refreshable token is invalid")
    }

    @Test
    fun testSignInSignOutAndTryToCreateAccessTokenWithInitialRefreshToken() = runTest {
        val authenticationsRoutes = AuthenticationsRoutesTestImpl()
        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        UserAcceptance.createUser(username, password)

        val signInResult = authenticationsRoutes.signIn(
            UsernamePasswordAuthenticationRequest(
                username,
                password
            )
        )

        val accessTokenResponse = authenticationsRoutes.createAccessToken(
            listOf(CookieRequest(signInResult.first[0].name, signInResult.first[0].value))
        )
        assertThat(accessTokenResponse.accessToken)
            .isNotEqualTo(signInResult.second.accessToken)
        assertThat(signInResult.first)
            .hasSize(1)

        val signOutCookie = authenticationsRoutes.signOut(
            listOf(
                CookieRequest(
                    signInResult.first[0].name,
                    signInResult.first[0].value
                )
            )
        )
        assertThat(signOutCookie)
            .isEmpty()

        val e = assertThrows<RestErrorException> {
            authenticationsRoutes.createAccessToken(
                listOf(CookieRequest(signInResult.first[0].name, signInResult.first[0].value))
            )
        }
        assertAuthenticationFailed(e, "Refreshable token is expired")
    }

    @Test
    fun testAuthenticateAsSuperAdmin() = runTest {
        val signInResult = authenticationsRoutes.signIn(
            UsernamePasswordAuthenticationRequest(
                "super_admin",
                "super_admin"
            )
        )
        assertThat(signInResult.first)
            .hasSize(1)
            .satisfies({
                assertThat(it[0].value).hasSize(64)
                assertThat(it[0].name).isEqualTo("refreshable_token")
                assertThat(Instant.ofEpochMilli(it[0].expires!!.timestamp))
                    .isCloseTo(Instant.now().plus(2, ChronoUnit.DAYS), within(1, ChronoUnit.MINUTES))
                assertThat(it[0].path).isEqualTo("/")
                assertThat(it[0].httpOnly).isTrue()
            })
        val accessTokenResponse = signInResult.second

        assertThat(accessTokenResponse.refreshTokenId).hasSize(36)
        assertThat(accessTokenResponse.accessTokenId).hasSize(36)
        assertThat(accessTokenResponse.accessToken).hasSize(64)
        assertThat(accessTokenResponse.authorizationScopes).isEqualTo(
            setOf(
                AuthorizationScopeResponse.REGULAR_USER,
                AuthorizationScopeResponse.ADMIN,
                AuthorizationScopeResponse.SUPER_ADMIN
            )
        )

        val refreshedAccessToken = authenticationsRoutes.createAccessToken(
            listOf(CookieRequest(signInResult.first[0].name, signInResult.first[0].value))
        )
        assertThat(refreshedAccessToken.refreshTokenExpirationDate.epochSeconds)
            .isEqualTo(signInResult.first[0].expires!!.timestamp / 1000) // millis part of GMTDate is 000 all the time
        assertThat(Instant.ofEpochMilli(refreshedAccessToken.accessTokenExpirationDate.toEpochMilliseconds()))
            .isCloseTo(Instant.now().plus(10, ChronoUnit.MINUTES), within(1, ChronoUnit.MINUTES))
        assertThat(refreshedAccessToken.accessToken).hasSize(64)
        assertThat(refreshedAccessToken.accessToken).isNotEqualTo(accessTokenResponse.accessToken)
        assertThat(refreshedAccessToken.authorizationScopes).isEqualTo(
            setOf(
                AuthorizationScopeResponse.REGULAR_USER,
                AuthorizationScopeResponse.ADMIN,
                AuthorizationScopeResponse.SUPER_ADMIN
            )
        )
    }

    @CsvSource(
        value = arrayOf(
            "'admin1', 'admin1'",
            "'admin', 'admin1'",
            "'admin1', 'admin'",
            "'admin', ''",
            "'', 'admin'",
            "'', ''",
        ), quoteCharacter = '\''
    )
    @ParameterizedTest
    fun testAuthenticateWithInvalidUsernameOrPassword(username: String, password: String) = runTest {
        val e = assertThrows<RestErrorException> {
            authenticationsRoutes.signIn(
                UsernamePasswordAuthenticationRequest(
                    username,
                    password
                )
            )
        }
        assertAuthenticationFailed(e, "Username of password is invalid")
    }

    private fun assertAuthenticationFailed(e: RestErrorException, expectedReason: String) {
        assertThat(e.httpStatusCode).isEqualTo(401)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("authentication_error")
            assertThat(it.message).isEqualTo("Authentication failed")
            assertThat(it.data).isEqualTo(
                mapOf(
                    "reason" to expectedReason
                )
            )
        })
    }

    suspend fun realDelayFunction(durationMs: Long): String = withContext(Dispatchers.Default) {
        delay(durationMs)
        "done"
    }
}
