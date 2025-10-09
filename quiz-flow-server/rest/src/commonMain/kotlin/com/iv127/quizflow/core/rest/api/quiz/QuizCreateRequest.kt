package com.iv127.quizflow.core.rest.api.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizCreateRequest(
    @SerialName("question_set_id") val questionSetId: String,
    @SerialName("question_set_version") val questionSetVersion: Int,
    @SerialName("question_ids") val questionIds: List<String>,
)

