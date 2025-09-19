package com.iv127.quizflow.core.rest.healthcheck

import com.iv127.quizflow.core.application.ApplicationState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthCheckResponse(@SerialName("state") val state: ApplicationState)
