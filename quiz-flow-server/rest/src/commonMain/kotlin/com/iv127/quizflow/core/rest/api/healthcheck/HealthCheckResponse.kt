package com.iv127.quizflow.core.rest.api.healthcheck

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthCheckResponse(@SerialName("state") val state: ApplicationStateResponse)
