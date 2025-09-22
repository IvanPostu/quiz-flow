package com.iv127.quizflow.core.rest.question

import com.iv127.quizflow.core.ktor.Multipart
import com.iv127.quizflow.core.ktor.MultipartPart
import com.iv127.quizflow.core.model.question.QuestionSet
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolver
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolverFactory
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolverType
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import io.ktor.server.request.contentType
import io.ktor.server.request.receiveChannel
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.post
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named

class QuestionsRoutes(val koinApp: KoinApplication) : ApiRoute {
    companion object {
        private val ROUTE_PATH: String = "/question-sets/{question_set_id}/questions"
    }

    private val db: () -> SqliteDatabase = {
        koinApp.koin.get<SqliteDatabase>(named("appDb"))
    }
    private val questionResolver: QuestionsResolver = QuestionsResolverFactory()
        .create(QuestionsResolverType.QUESTION_WRAPPED_IN_MARKDOWN_CODE_SECTION)

    override fun setup(parent: Route) {
        parent.post(ROUTE_PATH, webResponse {
            val questionSetId = call.parameters["question_set_id"]
                ?: throw IllegalArgumentException("question_set_id pathParam is empty")
            val response = upload(this, questionSetId)
            JsonWebResponse.create(response)
        })
    }

    private suspend fun upload(context: RoutingContext, questionSetId: String): List<QuestionResponse> {
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

        db().use { db ->
            db.executeAndGetChangedRowsCount("BEGIN TRANSACTION;")
            val resultRecords = db.executeAndGetResultSet(
                """
                    SELECT 
                        t.id,
                        t.created_at,
                        t.archived_at,
                        t.json 
                    FROM questions_set AS t
                    WHERE t.id=?;
                """
                    .trimIndent(),
                listOf(questionSetId)
            )

            if (resultRecords.isEmpty()) {
                throw IllegalStateException("Can't find question set with id $questionSetId")
            }

            val questionSet: QuestionSet = Json.decodeFromString<QuestionSet>(resultRecords[0]["json"].toString())
            val questionSetWithQuestions = QuestionSet(
                questionSet.id, questionSet.name, questionSet.description, questions
            )
            db.executeAndGetChangedRowsCount(
                """
                        UPDATE questions_set SET json=?
                        WHERE questions_set.id=${questionSet.id};
                    """.trimIndent(),
                listOf(Json.encodeToString(questionSetWithQuestions))
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            return questions
                .map {
                    QuestionResponseMapper.mapToResponse(it)
                }.toList()
        }
    }

}
