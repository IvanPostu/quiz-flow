package com.iv127.quizflow.api.automation.tests.route

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class QuestionSetsRouteTest {

    @Test
    fun `test create a question set`() = runTest {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }
        val response: io.ktor.client.statement.HttpResponse = client.get("http://localhost:8080/api/question-sets") {
            contentType(ContentType.Application.Json)
        }

        val responseBody: String = response.body()
        println(responseBody)

        client.close()
    }


}
