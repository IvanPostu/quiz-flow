package com.iv127.quizflow.core.rest.api.quizresult

import com.iv127.quizflow.core.model.authorization.Authorization
import com.iv127.quizflow.core.model.quizz.Quiz
import com.iv127.quizflow.core.model.quizz.QuizNotFoundException
import com.iv127.quizflow.core.model.quizz.QuizQuestion
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.SortOrder
import com.iv127.quizflow.core.rest.api.authorization.ApiAuthorization
import com.iv127.quizflow.core.rest.api.quizresult.QuizResultsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.api.toSortOrderEnumOrNull
import com.iv127.quizflow.core.rest.impl.exception.ApiClientErrorExceptionTranslator
import com.iv127.quizflow.core.rest.impl.quizresult.FinalizedQuizNotFoundException
import com.iv127.quizflow.core.security.AuthenticationProvider
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import com.iv127.quizflow.core.services.quiz.QuizService
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlin.time.ExperimentalTime
import org.koin.core.KoinApplication

@OptIn(ExperimentalTime::class)
class QuizResultsRoutesImpl(koinApp: KoinApplication) : QuizResultsRoutes, ApiRoute {

    private val quizService: QuizService by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.get("$ROUTE_PATH/{quizId}", routingContextWebResponse {
            val authorization = AuthenticationProvider.provide(call)
            val quizId = call.parameters["quizId"] ?: ""
            JsonWebResponse.create(get(authorization, quizId))
        })
        parent.get(ROUTE_PATH, routingContextWebResponse {
            val limit: Int? = call.request.queryParameters["limit"]?.toIntOrNull()
            val offset: Int? = call.request.queryParameters["offset"]?.toIntOrNull()
            val sortOrder: SortOrder? = call.request.queryParameters["sortOrder"].toSortOrderEnumOrNull()
            val authorization = AuthenticationProvider.provide(call)
            JsonWebResponse.create(list(authorization, offset, limit, sortOrder))
        })
    }

    override suspend fun get(authorization: ApiAuthorization, quizId: String): QuizResultResponse {
        try {
            val quiz = getFinalizedQuiz(authorization as Authorization, quizId)
            return mapToQuizResultResponse(quiz)
        } catch (e: FinalizedQuizNotFoundException) {
            throw ApiClientErrorExceptionTranslator.translateAndThrowOrElseFail(
                e,
                FinalizedQuizNotFoundException::class,
            )
        }
    }

    override suspend fun list(
        authorization: ApiAuthorization,
        offset: Int?,
        limit: Int?,
        sortOrder: SortOrder?
    ): List<QuizResultResponse> {
        val explicitOffset = offset ?: 0
        val explicitLimit = limit ?: 10
        val explicitSortOrder = sortOrder ?: SortOrder.DESC

        val finalizedQuizzes = quizService.getQuizList(
            authorization as Authorization,
            explicitOffset,
            explicitLimit,
            explicitSortOrder,
            true
        )
        return finalizedQuizzes.map { mapToQuizResultResponse(it) }
    }

    private fun mapToQuizResultResponse(quiz: Quiz): QuizResultResponse {
        val questionsById: Map<String, QuizQuestion> = quiz.quizQuestions.associateBy { it.questionId }

        var answersCount = 0
        var correctAnswersCount = 0

        for (quizAnswer in quiz.quizAnswers) {
            answersCount++
            val correctAnswerIndexes = questionsById[quizAnswer.questionId]!!.correctAnswerIndexes.toSet()
            val chosenAnswerIndexes = quizAnswer.chosenAnswerIndexes.toSet()
            if (correctAnswerIndexes == chosenAnswerIndexes) {
                correctAnswersCount++
            }
        }

        return QuizResultResponse(
            quiz.id,
            quiz.questionSetId,
            quiz.questionSetVersion,
            quiz.createdDate,
            quiz.finalizedDate!!,
            questionsById.size,
            answersCount,
            correctAnswersCount,
            quiz.quizAnswers.map {
                QuizResultAnswerResponse(
                    it.questionId,
                    it.chosenAnswerIndexes,
                    questionsById[it.questionId]!!.correctAnswerIndexes
                )
            }
        )
    }

    private fun getFinalizedQuiz(authorization: Authorization, quizId: String): Quiz {
        try {
            val quiz = quizService.getQuiz(authorization, quizId)
            if (!quiz.isFinalized()) {
                throw FinalizedQuizNotFoundException(quizId, "quiz is not finalized")
            }
            return quiz
        } catch (e: QuizNotFoundException) {
            throw FinalizedQuizNotFoundException(quizId, "invalid quizId")
        }
    }

}

