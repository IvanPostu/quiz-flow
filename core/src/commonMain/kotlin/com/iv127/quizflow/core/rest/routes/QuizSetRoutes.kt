package com.iv127.quizflow.core.rest.routes

import com.iv127.quizflow.core.rest.requests.QuizSetCreateRequest
import com.iv127.quizflow.core.rest.requests.QuizSetUpdateRequest
import com.iv127.quizflow.core.rest.responses.HealthCheckResponse
import com.iv127.quizflow.core.rest.responses.QuizSetResponse
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class QuizSetRoutes : ApiRoute {

    companion object {
        private val ROUTE_PATH: String = "/quiz-set"
    }

    override fun setup(parent: Route) {
        parent.get(
            ROUTE_PATH,
            webResponse {
                JsonWebResponse.create(HealthCheckResponse("SUCCESS"))
            }
        )
    }

    private fun get(id: String): QuizSetResponse {
        TODO()
    }

    private fun list(): List<QuizSetResponse> {
        TODO()
    }

    private fun create(request: QuizSetCreateRequest): QuizSetResponse {
        TODO()
    }

    private fun update(request: QuizSetUpdateRequest): QuizSetResponse {
        TODO()
    }

    private fun delete(id: String): QuizSetResponse {
        TODO()
    }

}
