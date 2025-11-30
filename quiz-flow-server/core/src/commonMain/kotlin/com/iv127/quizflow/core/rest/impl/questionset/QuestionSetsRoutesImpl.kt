package com.iv127.quizflow.core.rest.impl.questionset

import com.iv127.quizflow.core.model.authentication.AuthorizationScope
import com.iv127.quizflow.core.model.question.QuestionSet
import com.iv127.quizflow.core.model.question.QuestionSetNotFoundException
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.SortOrder
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetResponse
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetUpdateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.impl.exception.ApiClientErrorExceptionTranslator
import com.iv127.quizflow.core.security.AccessTokenProvider
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import com.iv127.quizflow.core.services.authentication.AuthenticationService
import com.iv127.quizflow.core.services.questionset.QuestionSetService
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlin.time.ExperimentalTime
import org.koin.core.KoinApplication

@OptIn(ExperimentalTime::class)
class QuestionSetsRoutesImpl(koinApp: KoinApplication) : QuestionSetsRoutes, ApiRoute {

    private val questionSetService: QuestionSetService by koinApp.koin.inject()
    private val authenticationService: AuthenticationService by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.get("$ROUTE_PATH/{id}", routingContextWebResponse {
            val accessToken = AccessTokenProvider.provide(call)
            val id = call.parameters["id"] ?: throw IllegalArgumentException("id pathParam is empty")
            JsonWebResponse.create(get(accessToken, id))
        })
        parent.get(ROUTE_PATH, routingContextWebResponse {
            val accessToken = AccessTokenProvider.provide(call)
            val limit: Int = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            val offset: Int = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            val sortOrder: SortOrder = SortOrder.valueOf(call.request.queryParameters["sortOrder"] ?: "ASC")
            JsonWebResponse.create(list(accessToken, offset, limit, sortOrder))
        })
        parent.get("$ROUTE_PATH/global", routingContextWebResponse {
            val accessToken = AccessTokenProvider.provide(call)
            val limit: Int = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            val offset: Int = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            val sortOrder: SortOrder = SortOrder.valueOf(call.request.queryParameters["sortOrder"] ?: "ASC")
            JsonWebResponse.create(listGlobal(accessToken, offset, limit, sortOrder))
        })
        parent.post(ROUTE_PATH, routingContextWebResponse {
            val accessToken = AccessTokenProvider.provide(call)
            val request = call.receive<QuestionSetCreateRequest>()
            JsonWebResponse.create(create(accessToken, request))
        })
        parent.delete("$ROUTE_PATH/{id}", routingContextWebResponse {
            val accessToken = AccessTokenProvider.provide(call)
            val id = call.parameters["id"] ?: throw IllegalArgumentException("id pathParam is empty")
            JsonWebResponse.create(archive(accessToken, id))
        })
        parent.put("$ROUTE_PATH/{id}", routingContextWebResponse {
            val accessToken = AccessTokenProvider.provide(call)
            val id = call.parameters["id"] ?: throw IllegalArgumentException("id pathParam is empty")
            val request = call.receive<QuestionSetUpdateRequest>()
            JsonWebResponse.create(update(accessToken, id, request))
        })
    }

    override suspend fun get(accessToken: String, id: String): QuestionSetResponse {
        val authorization = authenticationService.getAuthenticationByAccessToken(accessToken)
        authenticationService.checkAuthorizationScopes(authorization, setOf(AuthorizationScope.REGULAR_USER))
        try {
            return mapQuestionSetResponse(
                questionSetService.getQuestionSetWithVersionOrElseLatest(
                    authorization.authenticationRefreshToken.userId,
                    id,
                    null
                ).first
            )
        } catch (e: QuestionSetNotFoundException) {
            throw ApiClientErrorExceptionTranslator.translateAndThrowOrElseFail(
                e,
                QuestionSetNotFoundException::class,
            )
        }
    }

    override suspend fun list(
        accessToken: String,
        offset: Int,
        limit: Int,
        sortOrder: SortOrder
    ): List<QuestionSetResponse> {
        val authorization = authenticationService.getAuthenticationByAccessToken(accessToken)
        authenticationService.checkAuthorizationScopes(authorization, setOf(AuthorizationScope.REGULAR_USER))
        return questionSetService.getQuestionSetList(
            authorization.authenticationRefreshToken.userId,
            limit,
            offset,
            sortOrder
        )
            .map { mapQuestionSetResponse(it) }
    }

    override suspend fun listGlobal(
        accessToken: String,
        offset: Int,
        limit: Int,
        sortOrder: SortOrder
    ): List<QuestionSetResponse> {
        val authorization = authenticationService.getAuthenticationByAccessToken(accessToken)
        authenticationService.checkAuthorizationScopes(authorization, setOf(AuthorizationScope.REGULAR_USER))
        return questionSetService.getQuestionSetList(
            AuthenticationService.SUPER_USER_ID,
            limit,
            offset,
            sortOrder
        )
            .map { mapQuestionSetResponse(it) }
    }

    override suspend fun create(accessToken: String, request: QuestionSetCreateRequest): QuestionSetResponse {
        val authentication = authenticationService.getAuthenticationByAccessToken(accessToken)
        authenticationService.checkAuthorizationScopes(authentication, setOf(AuthorizationScope.REGULAR_USER))

        if (request.name.isBlank()) {
            throw IllegalArgumentException("name field shouldn't be blank")
        }
        val questionSet =
            questionSetService.createQuestionSet(authentication.authenticationRefreshToken.userId) { builder ->
                builder.name = request.name
                builder.description = request.description
            }.first
        return mapQuestionSetResponse(questionSet)
    }

    override suspend fun update(
        accessToken: String,
        id: String,
        request: QuestionSetUpdateRequest
    ): QuestionSetResponse {
        val authentication = authenticationService.getAuthenticationByAccessToken(accessToken)
        authenticationService.checkAuthorizationScopes(authentication, setOf(AuthorizationScope.REGULAR_USER))
        try {
            val questionSet =
                questionSetService.updateQuestionSet(authentication.authenticationRefreshToken.userId, id) { builder ->
                    builder.name = request.name
                    builder.description = request.description
                }.first
            return mapQuestionSetResponse(questionSet)
        } catch (e: QuestionSetNotFoundException) {
            throw ApiClientErrorExceptionTranslator.translateAndThrowOrElseFail(
                e,
                QuestionSetNotFoundException::class,
            )
        }
    }

    override suspend fun archive(accessToken: String, id: String): QuestionSetResponse {
        try {
            val authentication = authenticationService.getAuthenticationByAccessToken(accessToken)
            authenticationService.checkAuthorizationScopes(authentication, setOf(AuthorizationScope.REGULAR_USER))
            val questionSet = questionSetService.archive(authentication.authenticationRefreshToken.userId, id)
            return mapQuestionSetResponse(questionSet)
        } catch (e: QuestionSetNotFoundException) {
            throw ApiClientErrorExceptionTranslator.translateAndThrowOrElseFail(
                e,
                QuestionSetNotFoundException::class,
            )
        }
    }

    private fun mapQuestionSetResponse(questionSet: QuestionSet) = QuestionSetResponse(
        questionSet.id,
        questionSet.name,
        questionSet.description,
        questionSet.latestVersion,
        questionSet.createdDate
    )
}
