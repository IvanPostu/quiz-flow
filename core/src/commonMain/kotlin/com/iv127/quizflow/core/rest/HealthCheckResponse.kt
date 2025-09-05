package com.iv127.quizflow.core.rest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthCheckResponse(@SerialName("status") private val status: String)
