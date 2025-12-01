package com.iv127.quizflow.core.rest.api.quizresult

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizResultAnswerResponse(
    @SerialName("question_id") val questionId: String,
    @SerialName("chosen_answer_indexes") val chosenAnswerIndexes: List<Int>,
    @SerialName("right_answer_indexes") val rightAnswerIndexes: List<Int>?,
)
