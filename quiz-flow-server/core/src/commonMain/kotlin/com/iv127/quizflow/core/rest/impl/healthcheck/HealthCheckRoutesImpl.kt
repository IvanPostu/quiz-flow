package com.iv127.quizflow.core.rest.impl.healthcheck

import com.iv127.quizflow.core.ApplicationStateListener
import com.iv127.quizflow.core.application.ApplicationState
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.api.healthcheck.ApplicationStateResponse
import com.iv127.quizflow.core.rest.api.healthcheck.HealthCheckResponse
import com.iv127.quizflow.core.rest.api.healthcheck.HealthCheckRoutes
import com.iv127.quizflow.core.rest.api.healthcheck.HealthCheckRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.server.JsonWebResponse
import com.iv127.quizflow.core.server.routingContextWebResponse
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.core.KoinApplication

class HealthCheckRoutesImpl(koinApp: KoinApplication) : HealthCheckRoutes, ApiRoute {

    private val stateListener: ApplicationStateListener by koinApp.koin.inject()

    override fun setup(parent: Route) {
        parent.get(
            ROUTE_PATH,
            routingContextWebResponse {
                JsonWebResponse.create(get())
            }
        )
    }

    override fun get(): HealthCheckResponse {
        return HealthCheckResponse(mapStateToResponse(stateListener.getActualState()))
    }


    private fun mapStateToResponse(state: ApplicationState): ApplicationStateResponse {
        return when (state) {
            ApplicationState.APPLICATION_STARTING -> ApplicationStateResponse.APPLICATION_STARTING
            ApplicationState.APPLICATION_MODULES_LOADING -> ApplicationStateResponse.APPLICATION_MODULES_LOADING
            ApplicationState.APPLICATION_MODULES_LOADED -> ApplicationStateResponse.APPLICATION_MODULES_LOADED
            ApplicationState.APPLICATION_STARTED -> ApplicationStateResponse.APPLICATION_STARTED
            ApplicationState.SERVER_READY -> ApplicationStateResponse.SERVER_READY
            ApplicationState.APPLICATION_STOP_PREPARING -> ApplicationStateResponse.APPLICATION_STOP_PREPARING
            ApplicationState.APPLICATION_STOPPING -> ApplicationStateResponse.APPLICATION_STOPPING
            ApplicationState.APPLICATION_STOPPED -> ApplicationStateResponse.APPLICATION_STOPPED
        }
    }

}
