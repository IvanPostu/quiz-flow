package com.iv127.quizflow.core.rest.api.quiz

import com.iv127.quizflow.core.rest.api.InstantSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalTime::class)
@Serializable
data class QuizResponse(
    @SerialName("id") val id: String,
    @SerialName("question_set_id") val questionSetId: String,
    @SerialName("question_set_version") val questionSetVersion: Int,
    @SerialName("created_date") @Serializable(with = InstantSerializer::class) val createdDate: Instant,
    @SerialName("finalized_date") @Serializable(with = InstantSerializer::class) val finalizedDate: Instant?,
    @SerialName("is_finalized") val isFinalized: Boolean,
    @SerialName("questions") val questions: List<QuizQuestionResponse>,
)
 
