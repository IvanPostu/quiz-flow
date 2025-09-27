package com.iv127.quizflow.core.rest.impl.question

import com.iv127.quizflow.core.ktor.Multipart
import com.iv127.quizflow.core.ktor.MultipartPart
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolver
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolverFactory
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolverType
import com.iv127.quizflow.core.rest.api.question.QuestionResponse
import com.iv127.quizflow.core.rest.api.question.QuestionsRoutes
import com.iv127.quizflow.core.rest.api.question.QuestionsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import com.iv127.quizflow.core.services.QuestionSetService
import io.ktor.server.request.contentType
import io.ktor.server.request.receiveChannel
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import org.koin.core.KoinApplication

class QuestionsRoutesImpl(koinApp: KoinApplication) : QuestionsRoutes {

    private val questionSetService: QuestionSetService by koinApp.koin.inject()

    private val questionResolver: QuestionsResolver = QuestionsResolverFactory()
        .create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)

    override fun setup(parent: Route) {
        parent.get("${ROUTE_PATH}/{question_id}", webResponse {
            val questionSetId = call.parameters["question_set_id"]
                ?: throw IllegalArgumentException("question_set_id pathParam is empty")
            val questionId =
                call.parameters["question_id"] ?: throw IllegalArgumentException("question_id pathParam is empty")
            JsonWebResponse.create(get(questionSetId, questionId))
        })
        parent.get(ROUTE_PATH, webResponse {
            val questionSetId = call.parameters["question_set_id"]
                ?: throw IllegalArgumentException("question_set_id pathParam is empty")
            JsonWebResponse.create(list(questionSetId))
        })
        parent.post(ROUTE_PATH, webResponse {
            val questionSetId = call.parameters["question_set_id"]
                ?: throw IllegalArgumentException("question_set_id pathParam is empty")
            val response = upload(this, questionSetId)
            JsonWebResponse.create(response)
        })
    }

    override fun list(questionSetId: String): List<QuestionResponse> {
        return questionSetService.getQuestionSetWithVersionOrElseLatest(questionSetId, null)
            .second
            .questions.map {
                QuestionResponseMapper.mapToResponse(it)
            }
    }

    override fun get(questionSetId: String, questionId: String): QuestionResponse {
        val question = questionSetService.getQuestionSetWithVersionOrElseLatest(questionSetId, null)
            .second
            .questions
            .find { it.id == questionId }
        return QuestionResponseMapper.mapToResponse(question!!)
    }

    override suspend fun upload(context: RoutingContext, questionSetId: String): List<QuestionResponse> {
        val contentType = context.call.request.contentType()
        val boundary = contentType.parameter("boundary") ?: throw IllegalArgumentException("Boundary not found")
        val channel: ByteReadChannel = context.call.receiveChannel()
        val rawBody = channel.readRemaining().readByteArray()
        val multipartData = Multipart.parseMultipart(rawBody, boundary)

        val filePart = multipartData.parts.first {
            it is MultipartPart.FilePart && it.name == "file"
        } as MultipartPart.FilePart

        val questions = questionResolver.resolve(filePart.content.decodeToString())
            .asResult()
            .getOrThrow()

        return questionSetService.updateQuestionSet(questionSetId) {
            it.setQuestions(questions)
        }.second.questions.map {
            QuestionResponseMapper.mapToResponse(it)
        }
    }

}
