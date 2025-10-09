package com.iv127.quizflow.core.model.quizz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizAnswer(
    @SerialName("question_id") val id: String,
    @SerialName("answer_indexes") val answerIndexes: List<Int>,
)
