package com.iv127.quizflow.server.acceptance.test.rest.impl

import com.iv127.quizflow.core.rest.api.MultipartData
import com.iv127.quizflow.core.rest.api.question.QuestionSetVersionResponse
import com.iv127.quizflow.core.rest.api.question.QuestionsRoutes
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class QuestionsRoutesTestImpl(
    private val config: GlobalConfig = GlobalConfig.INSTANCE
) : QuestionsRoutes {
    override suspend fun getQuestionSetVersion(
        accessToken: String,
        questionSetId: String,
        version: Int
    ): QuestionSetVersionResponse {
        val response: HttpResponse = config.performRequest { client ->
            val urlBasePart = "${QuestionsRoutes.ROUTE_PATH}/versions/$version"
                .replace(QuestionsRoutes.QUESTION_SET_ID_PLACEHOLDER, questionSetId)
            client.get("${config.baseUrl}/api$urlBasePart") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
            }
        }
        return response.body<QuestionSetVersionResponse>()
    }

    override suspend fun getQuestionSetVersion(accessToken: String, questionSetId: String): QuestionSetVersionResponse {
        val response: HttpResponse = config.performRequest { client ->
            val urlBasePart = QuestionsRoutes.ROUTE_PATH
                .replace(QuestionsRoutes.QUESTION_SET_ID_PLACEHOLDER, questionSetId)
            client.get("${config.baseUrl}/api$urlBasePart") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
            }
        }
        return response.body<QuestionSetVersionResponse>()
    }

    override suspend fun upload(
        accessToken: String,
        multipartDataList: List<MultipartData>,
        questionSetId: String
    ): QuestionSetVersionResponse {
        val response: HttpResponse = config.performRequest { client ->
            val urlBasePart = QuestionsRoutes.ROUTE_PATH
                .replace(QuestionsRoutes.QUESTION_SET_ID_PLACEHOLDER, questionSetId)
            client.post("${config.baseUrl}/api$urlBasePart") {
                contentType(ContentType.MultiPart.FormData)
                bearerAuth(accessToken)
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            for (value in multipartDataList) {
                                when (value) {
                                    is MultipartData.FormField -> {
                                        append(value.name, value.value)
                                    }

                                    is MultipartData.FilePart -> {
                                        append(value.name, value.content, Headers.build {
                                            append(
                                                HttpHeaders.ContentDisposition,
                                                "form-data; name=\"${value.name}\"; filename=\"${value.filename}\""
                                            )
                                        })
                                    }
                                }
                            }
                        }
                    )
                )
            }
        }
        return response.body<QuestionSetVersionResponse>()
    }
}
