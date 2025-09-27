package com.iv127.quizflow.core.rest.impl.questionset

import com.iv127.quizflow.core.model.question.QuestionSet
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetCreateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetResponse
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetUpdateRequest
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes
import com.iv127.quizflow.core.rest.api.questionset.QuestionSetsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import com.iv127.quizflow.core.services.QuestionSetService
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.core.KoinApplication

class QuestionSetsRoutesImpl(koinApp: KoinApplication) : QuestionSetsRoutes {

    private val questionSetService: QuestionSetService by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.get("$ROUTE_PATH/{id}", webResponse {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("id pathParam is empty")
            JsonWebResponse.create(get(id))
        })
        parent.get(ROUTE_PATH, webResponse {
            JsonWebResponse.create(list())
        })
        parent.post(ROUTE_PATH, webResponse {
            val request = call.receive<QuestionSetCreateRequest>()
            JsonWebResponse.create(create(request))
        })
        parent.delete("$ROUTE_PATH/{id}", webResponse {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("id pathParam is empty")
            JsonWebResponse.create(archive(id))
        })
        parent.put("$ROUTE_PATH/{id}", webResponse {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("id pathParam is empty")
            val request = call.receive<QuestionSetUpdateRequest>()
            JsonWebResponse.create(update(id, request))
        })
    }

    override fun get(id: String): QuestionSetResponse {
        return mapQuestionSetResponse(questionSetService.getQuestionSet(id))
    }

    override fun list(): List<QuestionSetResponse> {
        return questionSetService.getQuestionSetList()
            .map { mapQuestionSetResponse(it) }
    }

    override fun create(request: QuestionSetCreateRequest): QuestionSetResponse {
        if (request.name.isBlank()) {
            throw IllegalArgumentException("name field shouldn't be blank")
        }
        val questionSet = questionSetService.createQuestionSet { builder ->
            builder.name = request.name
            builder.description = request.description
        }.first
        return mapQuestionSetResponse(questionSet)
    }

    override fun update(id: String, request: QuestionSetUpdateRequest): QuestionSetResponse {
        val questionSet = questionSetService.updateQuestionSet(id) { builder ->
            builder.name = request.name
            builder.description = request.description
        }.first
        return mapQuestionSetResponse(questionSet)
    }

    override fun archive(id: String): QuestionSetResponse {
        val questionSet = questionSetService.archive(id)
        return mapQuestionSetResponse(questionSet)
    }

    private fun mapQuestionSetResponse(questionSet: QuestionSet) = QuestionSetResponse(
        questionSet.id,
        questionSet.name,
        questionSet.description,
        questionSet.latestVersion,
    )
}
