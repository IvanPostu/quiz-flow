package com.iv127.quizflow.core.rest.question

import com.iv127.quizflow.core.model.question.QuestionSet
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolver
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolverFactory
import com.iv127.quizflow.core.model.question.resolver.QuestionsResolverType
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.post
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.utils.io.readRemaining
import kotlin.time.ExperimentalTime
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named

@OptIn(ExperimentalTime::class)
class QuestionsRoutes(val koinApp: KoinApplication) : ApiRoute {

    companion object {
        private val ROUTE_PATH: String = "/question-sets/{question_set_id}/questions"
        private val LOG = KtorSimpleLogger(getClassFullName(QuestionsRoutes::class))
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
        val multipartData = context.call.receiveMultipart()
        val listOfByteArray = ArrayList<ByteArray>()
        var fileContent = ByteArray(0)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    LOG.debug("FormItem: ${part.name} = ${part.value}")
                }

                is PartData.FileItem -> {
                    val fileBytes = part.provider().readRemaining().readByteArray()
                    val fileName = part.originalFileName as String
                    listOfByteArray.add(fileBytes)

                    if (part.name == "file") {
                        fileContent = fileBytes
                        LOG.debug("$fileName - consumed: ${fileBytes.size} bytes")
                    }
                }

                else -> {}
            }
            part.dispose()
        }
        val questions = questionResolver.resolve(fileContent.decodeToString())
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
                    WHERE t.id=${questionSetId};
                """
                    .trimIndent()
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
                        UPDATE questions_set SET json='${Json.encodeToString(questionSet)}'
                        WHERE questions_set.id=${questionSet.id};
                    """.trimIndent()
            )
            db.executeAndGetChangedRowsCount("COMMIT TRANSACTION;")
            return questions
                .map {
                    QuestionResponse(
                        it.question,
                        it.answerOptions,
                        it.correctAnswerIndexes,
                        it.correctAnswerExplanation
                    )
                }.toList()
        }
    }

}
