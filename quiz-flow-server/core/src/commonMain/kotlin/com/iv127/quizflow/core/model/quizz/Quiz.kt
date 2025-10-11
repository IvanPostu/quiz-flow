package com.iv127.quizflow.core.model.quizz

import com.iv127.quizflow.core.rest.api.InstantSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalTime::class)
@Serializable
data class Quiz(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("question_set_id") val questionSetId: String,
    @SerialName("question_set_version") val questionSetVersion: Int,
    @SerialName("created_date") @Serializable(with = InstantSerializer::class) val createdDate: Instant,
    @SerialName("finalized_date") @Serializable(with = InstantSerializer::class) val finalizedDate: Instant?,
    @SerialName("quiz_questions") val quizQuestions: List<QuizQuestion>,
    @SerialName("quiz_answers") val quizAnswers: List<QuizAnswer>,
) {

    fun isFinalized(): Boolean = null != finalizedDate

}
