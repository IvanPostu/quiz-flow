package com.iv127.quizflow.server.acceptance.test.rest.security

import com.iv127.quizflow.core.rest.api.authorization.ApiAuthorization
import com.iv127.quizflow.core.rest.api.authorization.AuthorizationResponse

class ApiAuthorizationTestImpl : ApiAuthorization {

    private val accessToken: String

    constructor(authorizationResponse: AuthorizationResponse) {
        this.accessToken = authorizationResponse.accessToken
    }

    constructor(accessToken: String) {
        this.accessToken = accessToken
    }

    override fun getToken(): String = accessToken
}
