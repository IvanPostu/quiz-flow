package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.createApplicationModule
import com.iv127.quizflow.core.rest.healthcheck.HealthCheckResponse
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.Json

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            createApplicationModule(PlatformServicesImpl())()
        }
        client.get("/api/health-check").apply {
            assertEquals(HttpStatusCode.OK, status)
            val body = bodyAsText()

            assertNotNull(body)
            val deserialized: HealthCheckResponse = Json.decodeFromString(body)
            assertEquals("SUCCESS", deserialized.status)
        }
    }
}
