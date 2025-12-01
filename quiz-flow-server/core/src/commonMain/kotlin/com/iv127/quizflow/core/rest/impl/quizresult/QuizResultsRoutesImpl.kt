package com.iv127.quizflow.core.rest.api.quizresult

import com.iv127.quizflow.core.model.authentication.AuthorizationScope
import com.iv127.quizflow.core.model.quizz.Quiz
import com.iv127.quizflow.core.model.quizz.QuizNotFoundException
import com.iv127.quizflow.core.model.quizz.QuizQuestion
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.SortOrder
import com.iv127.quizflow.core.rest.api.quizresult.QuizResultsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.api.toSortOrderEnumOrNull
import com.iv127.quizflow.core.rest.impl.exception.ApiClientErrorExceptionTranslator
import com.iv127.quizflow.core.security.AccessTokenProvider
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import com.iv127.quizflow.core.services.authentication.AuthenticationService
import com.iv127.quizflow.core.services.quiz.QuizService
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlin.time.ExperimentalTime
import org.koin.core.KoinApplication

@OptIn(ExperimentalTime::class)
class QuizResultsRoutesImpl(koinApp: KoinApplication) : QuizResultsRoutes, ApiRoute {

    private val quizService: QuizService by koinApp.koin.inject()
    private val authenticationService: AuthenticationService by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.get("$ROUTE_PATH/{quizId}", routingContextWebResponse {
            val accessToken = AccessTokenProvider.provide(call)
            val quizId = call.parameters["quizId"] ?: ""
            JsonWebResponse.create(get(accessToken, quizId))
        })
        parent.get(ROUTE_PATH, routingContextWebResponse {
            val limit: Int? = call.request.queryParameters["limit"]?.toIntOrNull()
            val offset: Int? = call.request.queryParameters["offset"]?.toIntOrNull()
            val sortOrder: SortOrder? = call.request.queryParameters["sortOrder"].toSortOrderEnumOrNull()
            val quizFinalizedStateType: QuizFinalizedStateType? = call.request.queryParameters["quizFinalizedStateType"]
                .toQuizFinalizedStateTypeOrNull()
            val accessToken = AccessTokenProvider.provide(call)
            JsonWebResponse.create(list(accessToken, offset, limit, sortOrder, quizFinalizedStateType))
        })
    }

    override suspend fun get(accessToken: String, quizId: String): QuizResultResponse {
        val authorization = authenticationService.getAuthenticationByAccessToken(accessToken)
        authenticationService.checkAuthorizationScopes(authorization, setOf(AuthorizationScope.REGULAR_USER))

        try {
            val quiz = quizService.getQuiz(authorization.authenticationRefreshToken.userId, quizId)
            return mapToQuizResultResponse(quiz)
        } catch (e: QuizNotFoundException) {
            throw ApiClientErrorExceptionTranslator.translateAndThrowOrElseFail(
                e,
                QuizNotFoundException::class,
            )
        }
    }

    override suspend fun list(
        accessToken: String,
        offset: Int?,
        limit: Int?,
        sortOrder: SortOrder?,
        quizFinalizedStateType: QuizFinalizedStateType?,
    ): List<QuizResultResponse> {
        val authorization = authenticationService.getAuthenticationByAccessToken(accessToken)
        authenticationService.checkAuthorizationScopes(authorization, setOf(AuthorizationScope.REGULAR_USER))

        val explicitOffset = offset ?: 0
        val explicitLimit = limit ?: 10
        val explicitSortOrder = sortOrder ?: SortOrder.DESC

        val finalizedStateType =
            if (quizFinalizedStateType == null) QuizService.FinalizedStateType.ALL else QuizService.FinalizedStateType.valueOf(
                quizFinalizedStateType.name
            )
        val finalizedQuizzes = quizService.getQuizList(
            authorization.authenticationRefreshToken.userId,
            explicitOffset,
            explicitLimit,
            explicitSortOrder,
            finalizedStateType
        )
        return finalizedQuizzes.map { mapToQuizResultResponse(it) }
    }

    private fun mapToQuizResultResponse(quiz: Quiz): QuizResultResponse {
        val questionsById: Map<String, QuizQuestion> = quiz.quizQuestions.associateBy { it.questionId }
        val isFinalized = quiz.finalizedDate != null

        var answersCount = 0
        var correctAnswersCount = 0

        for (quizAnswer in quiz.quizAnswers) {
            val correctAnswerIndexes = questionsById[quizAnswer.questionId]!!.correctAnswerIndexes.toSet()
            val chosenAnswerIndexes = quizAnswer.chosenAnswerIndexes.toSet()
            if (chosenAnswerIndexes.isNotEmpty()) {
                answersCount++
            }
            if (correctAnswerIndexes == chosenAnswerIndexes) {
                correctAnswersCount++
            }
        }

        return QuizResultResponse(
            quiz.id,
            quiz.questionSetId,
            quiz.questionSetVersion,
            quiz.createdDate,
            quiz.finalizedDate,
            questionsById.size,
            answersCount,
            if (isFinalized) correctAnswersCount else null,
            quiz.quizAnswers.map {
                QuizResultAnswerResponse(
                    it.questionId,
                    it.chosenAnswerIndexes,
                    if (isFinalized) questionsById[it.questionId]!!.correctAnswerIndexes else null
                )
            }
        )
    }
}

