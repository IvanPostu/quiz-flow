package com.iv127.quizflow.webapp

import com.iv127.quizflow.core.quizFlowApplicationModule
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            quizFlowApplicationModule()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertNotNull(bodyAsText())
        }
    }
}
