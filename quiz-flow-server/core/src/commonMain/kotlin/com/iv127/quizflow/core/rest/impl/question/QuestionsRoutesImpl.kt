package com.iv127.quizflow.core.rest.impl.question

import com.iv127.quizflow.core.ktor.Multipart
import com.iv127.quizflow.core.model.question.InvalidQuestionSetVersionException
import com.iv127.quizflow.core.model.question.QuestionSetNotFoundException
import com.iv127.quizflow.core.model.question.QuestionSetVersion
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolver
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolverFactory
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolverType
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.MultipartData
import com.iv127.quizflow.core.rest.api.question.QuestionSetVersionResponse
import com.iv127.quizflow.core.rest.api.question.QuestionsRoutes
import com.iv127.quizflow.core.rest.api.question.QuestionsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.impl.exception.ApiClientErrorExceptionTranslator
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import com.iv127.quizflow.core.services.questionset.QuestionSetService
import io.ktor.server.request.contentType
import io.ktor.server.request.receiveChannel
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import org.koin.core.KoinApplication

class QuestionsRoutesImpl(koinApp: KoinApplication) : QuestionsRoutes, ApiRoute {

    private val questionSetService: QuestionSetService by koinApp.koin.inject()

    private val questionResolver: QuestionsResolver = QuestionsResolverFactory()
        .create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)

    override fun setup(parent: Route) {
        parent.get("${ROUTE_PATH}/versions/{version}", routingContextWebResponse {
            val questionSetId = call.parameters["question_set_id"]
                ?: throw IllegalArgumentException("question_set_id pathParam is empty")
            val version = call.parameters["version"]
                ?: throw IllegalArgumentException("version pathParam is empty")
            JsonWebResponse.create(getQuestionSetVersion(questionSetId, version.toInt()))
        })
        parent.get(ROUTE_PATH, routingContextWebResponse {
            val questionSetId = call.parameters["question_set_id"]
                ?: throw IllegalArgumentException("question_set_id pathParam is empty")
            JsonWebResponse.create(getQuestionSetVersion(questionSetId))
        })
        parent.post(ROUTE_PATH, routingContextWebResponse {
            val questionSetId = call.parameters["question_set_id"]
                ?: throw IllegalArgumentException("question_set_id pathParam is empty")

            val contentType = this.call.request.contentType()
            val boundary = contentType.parameter("boundary") ?: throw IllegalArgumentException("Boundary not found")
            val channel: ByteReadChannel = this.call.receiveChannel()
            val rawBody = channel.readRemaining().readByteArray()
            val multipartData = Multipart.parseMultipart(rawBody, boundary)

            val response = upload(multipartData, questionSetId)
            JsonWebResponse.create(response)
        })
    }

    override suspend fun getQuestionSetVersion(questionSetId: String, version: Int): QuestionSetVersionResponse {
        try {
            val questionSetVersion = questionSetService.getQuestionSetWithVersionOrElseLatest(questionSetId, version)
                .second
            return mapQuestionSetVersionResponse(questionSetVersion)
        } catch (e: Exception) {
            throw ApiClientErrorExceptionTranslator.translateAndThrowOrElseFail(
                e,
                QuestionSetNotFoundException::class,
                InvalidQuestionSetVersionException::class
            )
        }
    }

    override suspend fun getQuestionSetVersion(questionSetId: String): QuestionSetVersionResponse {
        try {
            val questionSetVersion = questionSetService.getQuestionSetWithVersionOrElseLatest(questionSetId, null)
                .second
            return mapQuestionSetVersionResponse(questionSetVersion)
        } catch (e: QuestionSetNotFoundException) {
            throw ApiClientErrorExceptionTranslator
                .translateAndThrowOrElseFail(e, QuestionSetNotFoundException::class)
        }
    }

    override suspend fun upload(
        multipartDataList: List<MultipartData>,
        questionSetId: String
    ): QuestionSetVersionResponse {
        val filePart = multipartDataList.first {
            it is MultipartData.FilePart && it.name == "file"
        } as MultipartData.FilePart

        val questions = questionResolver.resolve(filePart.content.decodeToString())
            .asResult()
            .getOrThrow()

        val questionSetVersion: QuestionSetVersion = questionSetService.updateQuestionSet(questionSetId) {
            it.setQuestions(questions)
        }.second
        return mapQuestionSetVersionResponse(questionSetVersion)
    }

    private fun mapQuestionSetVersionResponse(questionSetVersion: QuestionSetVersion): QuestionSetVersionResponse =
        QuestionSetVersionResponse(
            questionSetVersion.id,
            questionSetVersion.version,
            questionSetVersion.questions.map { QuestionResponseMapper.mapToResponse(it) }
        )

}
