package com.iv127.quizflow.server.acceptance.test.route

import com.iv127.quizflow.core.rest.api.authentication.AuthorizationScopeResponse
import com.iv127.quizflow.core.rest.api.authentication.UsernamePasswordAuthenticationRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieRequest
import com.iv127.quizflow.server.acceptance.test.rest.impl.CookieAwareAuthenticationsRoutesTestImpl
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test

class AuthenticationRoutesTest {

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
                assertThat(it[0].path).isEqualTo("/authentications/access-token")
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
}
