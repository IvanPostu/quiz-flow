package com.iv127.quizflow.core.model.quizz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizAnswer(
    @SerialName("question_id") val questionId: String,
    @SerialName("chosen_answer_indexes") val chosenAnswerIndexes: List<Int>,
)
