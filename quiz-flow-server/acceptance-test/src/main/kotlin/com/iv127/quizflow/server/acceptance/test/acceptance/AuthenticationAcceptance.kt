package com.iv127.quizflow.server.acceptance.test.acceptance

import com.iv127.quizflow.core.rest.api.authentication.AccessTokenResponse
import com.iv127.quizflow.core.rest.api.authentication.UsernamePasswordAuthenticationRequest
import com.iv127.quizflow.server.acceptance.test.rest.impl.CookieAwareAuthenticationsRoutesTestImpl

object AuthenticationAcceptance {

    suspend fun authenticateSuperUser(): AccessTokenResponse {
        val cookieAwareAuthenticationsRoutesTestImpl = CookieAwareAuthenticationsRoutesTestImpl()
        val signInResult = cookieAwareAuthenticationsRoutesTestImpl.signIn(
            UsernamePasswordAuthenticationRequest(
                "super_admin",
                "super_admin"
            )
        )
        return signInResult.second
    }

    suspend fun authenticateUser(username: String, password: String): AccessTokenResponse {
        val cookieAwareAuthenticationsRoutesTestImpl = CookieAwareAuthenticationsRoutesTestImpl()
        val signInResult = cookieAwareAuthenticationsRoutesTestImpl.signIn(
            UsernamePasswordAuthenticationRequest(
                username,
                password
            )
        )
        return signInResult.second
    }

}
