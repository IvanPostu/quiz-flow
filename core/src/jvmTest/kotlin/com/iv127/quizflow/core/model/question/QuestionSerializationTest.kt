package com.iv127.quizflow.core.model.question

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

class QuestionSerializationTest {

    @Test
    fun testQuestionSerialization() {
        val question = Question("q?", listOf("a", "b"), listOf(0), "explanation")
        val serialized = Json.encodeToString(question)
        val deserialized = Json.decodeFromString<Question>(serialized)
        assertEquals(question, deserialized)
    }

}
