package com.iv127.quizflow.core.rest.api.authorization

interface AuthorizationRoutes {

    companion object {
        const val ROUTE_PATH: String = "/authorizations"
    }

    suspend fun authorize(usernamePasswordAuthorizationRequest: UsernamePasswordAuthorizationRequest): AuthorizationResponse

}
