package com.iv127.quizflow.server.acceptance.test.route

import com.iv127.quizflow.core.rest.api.authorization.AuthorizationScopeResponse
import com.iv127.quizflow.core.rest.api.authorization.UsernamePasswordAuthorizationRequest
import com.iv127.quizflow.server.acceptance.test.rest.impl.AuthorizationRoutesTestImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorizationRoutesTest {

    private lateinit var httpClient: HttpClient

    @BeforeEach
    fun setup() {
        httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }
    }

    @AfterEach
    fun tearDown() {
        httpClient.close()
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testAuthorizeAsAdmin() = runTest {
        val authorizationRoutes = AuthorizationRoutesTestImpl(httpClient)
        val adminAuthorization = authorizationRoutes.authorize(UsernamePasswordAuthorizationRequest("admin", "admin"))

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
