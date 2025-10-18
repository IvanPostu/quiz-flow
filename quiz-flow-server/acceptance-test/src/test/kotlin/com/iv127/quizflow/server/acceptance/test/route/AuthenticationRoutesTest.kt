package com.iv127.quizflow.server.acceptance.test.route

import com.iv127.quizflow.core.rest.api.authentication.AuthorizationScopeResponse
import com.iv127.quizflow.core.rest.api.authentication.UsernamePasswordAuthenticationRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieRequest
import com.iv127.quizflow.server.acceptance.test.rest.RestErrorException
import com.iv127.quizflow.server.acceptance.test.rest.impl.CookieAwareAuthenticationsRoutesTestImpl
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class AuthenticationRoutesTest {

    @Test
    fun testCreateAccessTokenWithoutRefreshableToken() = runTest {
        val cookieAwareAuthenticationsRoutesTestImpl = CookieAwareAuthenticationsRoutesTestImpl()

        val e = assertThrows<RestErrorException> {
            cookieAwareAuthenticationsRoutesTestImpl.createAccessToken(
                listOf()
            )
        }
        assertThat(e.httpStatusCode).isEqualTo(401)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("authentication_error")
            assertThat(it.message).isEqualTo("Authentication failed")
            assertThat(it.data).isEqualTo(
                mapOf(
                    "reason" to "Refresh token is not found in the cookie"
                )
            )
        })
    }

    @Test
    fun testCreateAccessTokenWithInvalidRefreshableToken() = runTest {
        val cookieAwareAuthenticationsRoutesTestImpl = CookieAwareAuthenticationsRoutesTestImpl()

        val e = assertThrows<RestErrorException> {
            cookieAwareAuthenticationsRoutesTestImpl.createAccessToken(
                listOf(
                    CookieRequest("refreshable_token", "whatever")
                )
            )
        }
        assertThat(e.httpStatusCode).isEqualTo(401)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("authentication_error")
            assertThat(it.message).isEqualTo("Authentication failed")
            assertThat(it.data).isEqualTo(
                mapOf(
                    "reason" to "Refreshable token is invalid"
                )
            )
        })
    }

    @Test
    fun testAuthenticateAsSuperAdmin() = runTest {
        val cookieAwareAuthenticationsRoutesTestImpl = CookieAwareAuthenticationsRoutesTestImpl()
        val signInResult = cookieAwareAuthenticationsRoutesTestImpl.signIn(
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
                assertThat(it[0].path).isEqualTo("/api/authentications/access-token")
                assertThat(it[0].httpOnly).isTrue()
            })
        val accessTokenResponse = signInResult.second

        assertThat(accessTokenResponse.accessToken).hasSize(64)
        assertThat(accessTokenResponse.authorizationScopes).isEqualTo(
            setOf(
                AuthorizationScopeResponse.REGULAR_USER,
                AuthorizationScopeResponse.ADMIN,
                AuthorizationScopeResponse.SUPER_ADMIN
            )
        )

        val refreshedAccessToken = cookieAwareAuthenticationsRoutesTestImpl.createAccessToken(
            listOf(CookieRequest(signInResult.first[0].name, signInResult.first[0].value))
        )
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
        val cookieAwareAuthenticationsRoutesTestImpl = CookieAwareAuthenticationsRoutesTestImpl()
        val e = assertThrows<RestErrorException> {
            cookieAwareAuthenticationsRoutesTestImpl.signIn(
                UsernamePasswordAuthenticationRequest(
                    username,
                    password
                )
            )
        }
        assertThat(e.httpStatusCode).isEqualTo(401)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("authentication_error")
            assertThat(it.message).isEqualTo("Authentication failed")
            assertThat(it.data).isEqualTo(
                mapOf(
                    "reason" to "Username of password is invalid"
                )
            )
        })
    }

}
