package com.iv127.quizflow.core.rest.healthcheck

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthCheckResponse(@SerialName("status") val status: String)
