package com.iv127.quizflow.server.acceptance.test.route

import com.iv127.quizflow.core.rest.api.authorization.AuthorizationScopeResponse
import com.iv127.quizflow.core.rest.api.authorization.UsernamePasswordAuthorizationRequest
import com.iv127.quizflow.server.acceptance.test.rest.RestErrorException
import com.iv127.quizflow.server.acceptance.test.rest.impl.AuthorizationsRoutesTestImpl
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@OptIn(ExperimentalTime::class)
class AuthorizationsRoutesTest {

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
    fun testAuthorizeWithInvalidUsernameOrPassword(username: String, password: String) = runTest {
        val authorizationRoutes = AuthorizationsRoutesTestImpl()
        val e = assertThrows<RestErrorException> {
            authorizationRoutes.authorize(UsernamePasswordAuthorizationRequest("admin1", "admin"))
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

    @Test
    fun testAuthorizeAsSuperAdmin() = runTest {
        val authorizationRoutes = AuthorizationsRoutesTestImpl()
        val adminAuthorization = authorizationRoutes
            .authorize(UsernamePasswordAuthorizationRequest("super_admin", "super_admin"))

        assertThat(adminAuthorization).satisfies({
            assertThat(it.id).isNotBlank()
            assertThat(it.accessToken).hasSize(64)
            assertThat(it.expirationDate.minus(it.createdDate)).isEqualTo(2.days)
            assertThat(it.userId).isEqualTo("902fb9f1-0199-1000-edfb-0000ad471c87")
            assertThat(it.scopes).containsExactlyInAnyOrder(
                AuthorizationScopeResponse.REGULAR_USER,
                AuthorizationScopeResponse.ADMIN,
                AuthorizationScopeResponse.SUPER_ADMIN,
            )
        })
    }
}
