package com.iv127.quizflow.core.rest.api.quizresult

import com.iv127.quizflow.core.rest.api.InstantSerializer
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalTime::class)
@Serializable
data class QuizResultResponse(
    @SerialName("quiz_id") val quizId: String,
    @SerialName("question_set_id") val questionSetId: String,
    @SerialName("question_set_name") val questionSetName: String,
    @SerialName("question_set_version") val questionSetVersion: Int,
    @SerialName("quiz_created_date") @Serializable(with = InstantSerializer::class) val quizCreatedDate: Instant,
    @SerialName("quiz_finalized_date") @Serializable(with = InstantSerializer::class) val quizFinalizedDate: Instant?,
    @SerialName("question_count") val questionsCount: Int,
    @SerialName("answers_count") val answersCount: Int,
    @SerialName("correct_answers_count") val correctAnswersCount: Int?,
    @SerialName("answers") val answers: List<QuizResultAnswerResponse>,
)

