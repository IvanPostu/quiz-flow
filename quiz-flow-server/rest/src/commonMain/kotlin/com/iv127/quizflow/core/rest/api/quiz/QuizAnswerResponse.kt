package com.iv127.quizflow.core.rest.api.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizAnswerResponse(
    @SerialName("question_id") val questionId: String,
    @SerialName("chosen_answer_indexes") val chosenAnswerIndexes: List<Int>,
)
