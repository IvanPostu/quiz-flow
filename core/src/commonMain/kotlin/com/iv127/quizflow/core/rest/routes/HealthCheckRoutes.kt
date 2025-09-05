package com.iv127.quizflow.core.rest.routes

import com.iv127.quizflow.core.rest.responses.HealthCheckResponse
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class HealthCheckRoutes : ApiRoute {
    override fun setup(parent: Route) {
        parent.get(
            "/health-check",
            webResponse {
                JsonWebResponse.create(HealthCheckResponse("SUCCESS"))
            }
        )
    }
}
