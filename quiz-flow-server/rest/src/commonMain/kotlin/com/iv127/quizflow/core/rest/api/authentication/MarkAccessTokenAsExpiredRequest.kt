package com.iv127.quizflow.core.rest.api.authentication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarkAccessTokenAsExpiredRequest(
    @SerialName("access_token_id") val accessTokenId: String
)
