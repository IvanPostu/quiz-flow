package com.iv127.quizflow.core.rest.routes

import com.iv127.quizflow.core.rest.HealthCheckResponse
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

class HealthCheckRoutes : ApiRoute {
    override fun setup(parent: Route) {
        parent.get(
            "/api/health-check",
            webResponse {
                JsonWebResponse.create(HealthCheckResponse("SUCCESS"))
            }
        )
    }
}
