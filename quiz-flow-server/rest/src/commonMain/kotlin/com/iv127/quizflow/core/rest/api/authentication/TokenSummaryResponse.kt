package com.iv127.quizflow.core.rest.api.authentication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokenSummaryResponse(
    @SerialName("refresh_token")
    val refreshTokenSummaryResponse: RefreshTokenSummaryResponse,
    @SerialName("access_tokens")
    val accessTokenResponse: List<AccessTokenResponse>,
)
