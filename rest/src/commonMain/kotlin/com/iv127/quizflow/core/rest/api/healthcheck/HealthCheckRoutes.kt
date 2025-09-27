package com.iv127.quizflow.core.rest.api.healthcheck

import com.iv127.quizflow.core.rest.api.ApiRoute

interface HealthCheckRoutes : ApiRoute {
    companion object {
        const val ROUTE_PATH: String = "/health-check"
    }

    fun get(): HealthCheckResponse
}
