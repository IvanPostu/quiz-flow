package com.iv127.quizflow.core.rest.api.healthcheck

interface HealthCheckRoutes {
    companion object {
        const val ROUTE_PATH: String = "/health-check"
    }

    fun get(): HealthCheckResponse
}
