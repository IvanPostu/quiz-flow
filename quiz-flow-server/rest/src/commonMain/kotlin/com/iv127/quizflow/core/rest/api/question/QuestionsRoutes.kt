package com.iv127.quizflow.core.rest.api.question

import com.iv127.quizflow.core.rest.api.MultipartData

interface QuestionsRoutes {
    companion object {
        const val QUESTION_SET_ID_PLACEHOLDER: String = "{question_set_id}"
        const val ROUTE_PATH: String = "/question-sets/$QUESTION_SET_ID_PLACEHOLDER/questions"
    }

    suspend fun getQuestionSetVersion(questionSetId: String, version: Int): QuestionSetVersionResponse

    suspend fun getQuestionSetVersion(questionSetId: String): QuestionSetVersionResponse

    suspend fun upload(multipartDataList: List<MultipartData>, questionSetId: String): QuestionSetVersionResponse

}
