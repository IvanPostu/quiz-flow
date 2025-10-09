package com.iv127.quizflow.core.rest.api.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizAnswerResponse(
    @SerialName("question_id") val questionId: String,
    @SerialName("question") val question: String,
    @SerialName("answer_options") val answerOptions: List<String>,
    @SerialName("chosen_answer_indexes") val chosenAnswerIndexes: List<Int>,
    @SerialName("right_answer_indexes") val rightAnswerIndexes: List<Int>,
    @SerialName("right_answer_explanation") val rightAnswerExplanation: String,
    @SerialName("score") val score: Int,
)
