package com.iv127.quizflow.core.rest.healthcheck

import com.iv127.quizflow.core.ApplicationStateListener
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.webResponse
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.core.KoinApplication

class HealthCheckRoutes(koinApp: KoinApplication) : ApiRoute {

    private val stateListener: ApplicationStateListener by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.get(
            "/health-check",
            webResponse {
                JsonWebResponse.create(HealthCheckResponse(stateListener.getActualState()))
            }
        )
    }
}
