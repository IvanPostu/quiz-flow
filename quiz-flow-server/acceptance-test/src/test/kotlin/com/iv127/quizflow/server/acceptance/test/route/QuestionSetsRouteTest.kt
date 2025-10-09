package com.iv127.quizflow.server.acceptance.test.route

import com.iv127.quizflow.core.rest.api.SortOrder
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetUpdateRequest
import com.iv127.quizflow.server.acceptance.test.rest.impl.QuestionSetsRoutesTestImpl
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QuestionSetsRouteTest {
    private val questionSetsRoutes = QuestionSetsRoutesTestImpl()

    @Test
    fun testCreateUpdateGetGetListAndDelete() = runTest {
        val createRequest = QuestionSetCreateRequest("Example of questionnaire", "Example of description")
        val created = questionSetsRoutes.create(createRequest)

        assertThat(created.latestVersion).isEqualTo(1)
        assertThat(created.name).isEqualTo("Example of questionnaire")
        assertThat(created.description).isEqualTo("Example of description")
        assertThat(created.id).isNotBlank()

        var questionSetFromList = questionSetsRoutes.list(0, Int.MAX_VALUE, SortOrder.ASC)
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

        questionSetFromList = questionSetsRoutes.list(0, Int.MAX_VALUE, SortOrder.ASC)
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
