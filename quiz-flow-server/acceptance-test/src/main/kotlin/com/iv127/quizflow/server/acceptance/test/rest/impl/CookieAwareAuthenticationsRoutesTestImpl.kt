package com.iv127.quizflow.server.acceptance.test.rest.impl

import com.iv127.quizflow.core.rest.api.authentication.AccessTokenResponse
import com.iv127.quizflow.core.rest.api.authentication.AccessTokenSummaryResponse
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.api.authentication.MarkAccessTokenAsExpiredRequest
import com.iv127.quizflow.core.rest.api.authentication.MarkRefreshTokenAsExpiredRequest
import com.iv127.quizflow.core.rest.api.authentication.RefreshTokenSummaryResponse
import com.iv127.quizflow.core.rest.api.authentication.TokenSummaryResponse
import com.iv127.quizflow.core.rest.api.authentication.UsernamePasswordAuthenticationRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieResponse
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.contentType

class CookieAwareAuthenticationsRoutesTestImpl(
    private val config: GlobalConfig = GlobalConfig.INSTANCE
) : AuthenticationsRoutes {

    companion object {
        private class CustomCookieStorage : CookiesStorage {
            private val cookies = ArrayList<Cookie>()

            override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
                cookies.add(cookie)
            }

            override suspend fun get(requestUrl: Url): List<Cookie> {
                return listOf()
            }

            override fun close() {
                cookies.clear()
            }

            fun getCookies(): List<Cookie> {
                return cookies.toList()
            }
        }
    }

    private val customCookieStorage = CustomCookieStorage()
    private val cookieAwareHttpClient = GlobalConfig.createConfiguredHttpClient({
        install(HttpCookies) {
            storage = customCookieStorage
        }
    })

    override suspend fun signIn(request: UsernamePasswordAuthenticationRequest): Pair<List<CookieResponse>, AccessTokenResponse> {
        val response: HttpResponse = config.performRequest(cookieAwareHttpClient) { client ->
            client.post("${config.baseUrl}/api${ROUTE_PATH}/sign-in") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
        val responseData: AccessTokenResponse = response.body<AccessTokenResponse>()
        val cookies = getCookies()
        return Pair(cookies.map { mapToCookieResponse(it) }, responseData)
    }

    override suspend fun createAccessToken(cookies: List<CookieRequest>): AccessTokenResponse {
        val cookieValue = cookies.map { "${it.name}=${it.value}" }
            .joinToString("; ")
        val response: HttpResponse = config.performRequest { client ->
            client.post("${config.baseUrl}/api${ROUTE_PATH}/access-token") {
                header("Test", "test")
                header("Cookie", cookieValue)
                contentType(ContentType.Application.Json)
                setBody(mapOf<String, String>())
            }
        }
        val responseData: AccessTokenResponse = response.body<AccessTokenResponse>()
        return responseData
    }

    override suspend fun extendAccessTokenLifetime(accessToken: String): AccessTokenResponse {
        val response: HttpResponse = config.performRequest { client ->
            client.post("${config.baseUrl}/api${ROUTE_PATH}/access-token-lifetime") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
                setBody(mapOf<String, String>())
            }
        }
        val responseData: AccessTokenResponse = response.body<AccessTokenResponse>()
        return responseData
    }

    override suspend fun markRefreshTokenAsExpired(
        accessToken: String,
        markRefreshTokenAsExpiredRequest: MarkRefreshTokenAsExpiredRequest
    ): RefreshTokenSummaryResponse {
        val response: HttpResponse = config.performRequest { client ->
            client.post("${config.baseUrl}/api${ROUTE_PATH}/refresh-token-expiration") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
                setBody(markRefreshTokenAsExpiredRequest)
            }
        }
        val responseData: RefreshTokenSummaryResponse = response.body<RefreshTokenSummaryResponse>()
        return responseData
    }

    override suspend fun markAccessTokenAsExpired(
        accessToken: String,
        markAccessTokenAsExpiredRequest: MarkAccessTokenAsExpiredRequest
    ): AccessTokenSummaryResponse {
        val response: HttpResponse = config.performRequest { client ->
            client.post("${config.baseUrl}/api${ROUTE_PATH}/access-token-expiration") {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
                setBody(markAccessTokenAsExpiredRequest)
            }
        }
        val responseData: AccessTokenSummaryResponse = response.body<AccessTokenSummaryResponse>()
        return responseData
    }

    override suspend fun getUserTokens(
        accessToken: String,
        markAccessTokenAsExpiredRequest: MarkAccessTokenAsExpiredRequest
    ): TokenSummaryResponse {
        TODO("Not yet implemented")
    }

    fun getCookies(): List<Cookie> = customCookieStorage.getCookies()

    private fun mapToCookieResponse(cookie: Cookie): CookieResponse {
        return CookieResponse(
            name = cookie.name,
            value = cookie.value,
            maxAge = cookie.maxAge,
            path = cookie.path,
            secure = cookie.secure,
            httpOnly = cookie.httpOnly,
            encoding = cookie.encoding,
            expires = cookie.expires,
            domain = cookie.domain,
            extensions = cookie.extensions
        )
    }
}
