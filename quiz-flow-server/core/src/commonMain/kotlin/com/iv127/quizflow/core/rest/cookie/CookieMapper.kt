package com.iv127.quizflow.core.rest.cookie

import com.iv127.quizflow.core.rest.api.cookie.CookieRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieResponse
import io.ktor.http.Cookie
import io.ktor.util.date.GMTDate

object CookieMapper {

    fun mapToCookieRequest(name: String, value: String): CookieRequest {
        return CookieRequest(name, value)
    }

    fun mapToCookieResponse(cookie: Cookie): CookieResponse {
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

    fun mapToExpiredCookie(cookie: CookieResponse): Cookie {
        return Cookie(
            name = cookie.name,
            value = "",
            maxAge = 0,
            expires = GMTDate.START,
            domain = cookie.domain,
            path = cookie.path,
            secure = cookie.secure,
            httpOnly = cookie.httpOnly,
            encoding = cookie.encoding,
            extensions = cookie.extensions
        )
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
