package com.iv127.quizflow.core.rest.cookie

import com.iv127.quizflow.core.rest.api.cookie.CookieRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieResponse
import io.ktor.http.Cookie

object CookieMapper {

    fun mapToCookieRequest(name: String, value: String): CookieRequest {
        return CookieRequest(name, value)
    }

    fun mapToCookie(cookieResponse: CookieResponse): Cookie {
        return Cookie(
            name = cookieResponse.name,
            value = cookieResponse.value,
            maxAge = cookieResponse.maxAge,
            path = cookieResponse.path,
            secure = cookieResponse.secure,
            httpOnly = cookieResponse.httpOnly,
            encoding = cookieResponse.encoding,
            expires = cookieResponse.expires,
            domain = cookieResponse.domain,
            extensions = cookieResponse.extensions
        )
    }

}
