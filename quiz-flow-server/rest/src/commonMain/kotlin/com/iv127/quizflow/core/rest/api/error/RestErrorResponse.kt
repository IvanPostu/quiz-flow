package com.iv127.quizflow.core.rest.api.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RestErrorResponse(
    @SerialName("unique_id") val uniqueId: String,
    @SerialName("error_code") val errorCode: String,
    @SerialName("message") val message: String,
    @SerialName("data") val data: Map<String, String>
)
