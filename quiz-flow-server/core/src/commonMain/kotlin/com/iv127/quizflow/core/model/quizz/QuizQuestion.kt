package com.iv127.quizflow.core.model.quizz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestion(
    @SerialName("question_id") val questionId: String,
    @SerialName("correct_answer_indexes") val correctAnswerIndexes: List<Int>,
)
