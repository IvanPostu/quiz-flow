package com.iv127.quizflow.api.automation.tests.route

import com.iv127.quizflow.api.automation.tests.rest.impl.QuestionSetsRoutesTestImpl
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetUpdateRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QuestionSetsRouteTest {

    private lateinit var client: HttpClient

    @BeforeEach
    fun setup() {
        client = HttpClient(CIO) {
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
        client.close()
    }

    @Test
    fun testCreateUpdateGetGetListAndDelete() = runTest {
        val questionSetsRoutes = QuestionSetsRoutesTestImpl(client)
        val createRequest = QuestionSetCreateRequest("Example of questionnaire", "Example of description")
        val created = questionSetsRoutes.create(createRequest)

        assertThat(created.latestVersion).isEqualTo(1)
        assertThat(created.name).isEqualTo("Example of questionnaire")
        assertThat(created.description).isEqualTo("Example of description")
        assertThat(created.id).isNotBlank()

        var questionSetFromList = questionSetsRoutes.list()
            .find { it.id == created.id }!!
        var questionSet = questionSetsRoutes.get(created.id)
        assertThat(listOf(questionSetFromList, questionSet))
            .allSatisfy {
                assertThat(it)
                    .usingRecursiveComparison()
                    .isEqualTo(created)
            }

        val updateRequest = QuestionSetUpdateRequest("Updated questionnaire", "Updated description")
        val updated = questionSetsRoutes.update(questionSet.id, updateRequest)
        assertThat(updated.latestVersion).isEqualTo(2)
        assertThat(updated.name).isEqualTo("Updated questionnaire")
        assertThat(updated.description).isEqualTo("Updated description")
        assertThat(updated.id)
            .isNotBlank()
            .isEqualTo(created.id)

        questionSetFromList = questionSetsRoutes.list()
            .find { it.id == updated.id }!!
        questionSet = questionSetsRoutes.get(updated.id)
        assertThat(listOf(questionSetFromList, questionSet))
            .allSatisfy {
                assertThat(it)
                    .usingRecursiveComparison()
                    .isEqualTo(updated)
            }
    }
}
