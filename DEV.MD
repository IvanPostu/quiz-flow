# Development related stuff

## Auth system

- 2 entities: Access Token, Refresh Token

- **Refresh Token**
  - Long Live, e.g. 1 year
  - should be secure: `Set-Cookie: refresh_token=abc123; HttpOnly; Secure; Path=/auth/refresh; SameSite=Strict`
    - HttpOnly blocks JS access to cookie
    - Secure requires https
    - Path: Cookie will only be sent to /auth/refresh requests
    - Max-Age (TTL in seconds) e.g. `Max-Age=604800;`
      - TTL of a refresh token should match the token's actual expiration set by your auth logic (e.g., JWT expiration or database record).
    - Using Expires (absolute date/time) `Expires=Sat, 25 Oct 2025 12:00:00 GMT;`
  - revocable (can be blacklisted)
  - rotatable (creation of a ne token invalidates the old one)
- **Access Token**
  - TTL e.g. 5-60 min.
  - JWT or Opaque(random string)
  - e.g. `Authorization: Bearer <access_token>`

## TODO

- Acceptance tests - rename GlobalConfig to GlobalContext
- Add static analyzer detekt
- Add formatter klint
- Add version explicit definition on build
- KSP (Kotlin Symbolic Processing) - annotations that allows generating classes before compile task
- Java impl should use the exactly same dynamic libraries that the native target uses
- Optimize font assets `npx ttf2woff2 LibraSans.ttf` (convert ttf to wof)
  - Font files shrink by 60–80%, removing the warnings entirely.
  - Convert .ttf → .woff2 (modern browsers)
  - Keep .woff as fallback if needed.

    ```sh
    @font-face {
    font-family: 'Libra Sans';
    src: url('./assets/LibraSans.woff2') format('woff2'),
        url('./assets/LibraSans.woff') format('woff');
    font-weight: normal;
    font-style: normal;
    }
    ```

```sh
git submodule add -b tags/version-3.50.4 git@github.com:sqlite/sqlite.git external/sqlite
git submodule add -b future git@github.com:rg3/libbcrypt.git external/libbcrypt

# Clone a Repo with Submodules
git submodule init
git submodule update

git submodule status 

git submodule update --remote external/sqlite
git submodule deinit -f external/sqlite
git rm --cached external/sqlite
rm -rf ./.git/modules/external/

./gradlew wrapper --gradle-version latest
```

## Upload file

```sh
curl \
    -H "Content-Type: multipart/form-data" \
    -F "testid=1" \
    -F "filecomment=This is an text file" \
    -F "file1=@/$PROJECT_ROOT/test.txt" \
    http://localhost:8080/api/upload

curl -X POST http://localhost:8080/api/upload2 \
    -H "Content-Type: multipart/form-data" \
    -F "testid=1" \
    -F "filecomment=This is an text file" \
    -F "file1=@/$PROJECT_ROOT/test.txt" 
    
```

## Requests

```sh
curl -i http://localhost:8080/api/health-check


# questions set endpoints
curl -i -X POST http://localhost:8080/api/question-sets -H "Content-Type: application/json" -d '{"name":"English 1","description":"Some simple english questions"}'

curl -X PUT http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7 -H "Content-Type: application/json" -d '{"name":"English Quiz","description":"A simple English language quiz"}' 

curl -i -X GET http://localhost:8080/api/question-sets -H "Content-Type: application/json"

curl -X GET http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7 -H "Content-Type: application/json"

curl -X DELETE http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7 -H "Content-Type: application/json"

# question-sets/{question_set_id}/questions endpoints
curl -X POST http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7/questions \
    -H "Content-Type: multipart/form-data" \
    -F "testid=1" \
    -F "filecomment=This is an text file" \
    -F "file=@/$PROJECT_ROOT/../docubase-documents/Java/OCP21/OCP21.MD"

curl -i -X GET http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7/questions/8afb9805-0199-1000-fbe4-00009ce065d8


# users
curl -X POST http://localhost:8080/api/users \
    -H "Authorization: Bearer af2ea80001991000d0ab00005a8a1466af2ea80001991000dbb30000cfeebf7a" \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"test"}'

curl -X POST http://localhost:8080/api/users \
    -H "Content-Type: application/json" \
    -d '{"username":"test1","password":"test1"}'

curl -X GET http://localhost:8080/api/users

curl \
    -F "filecomment=This is an text file" \
    -F "file=@/$PROJECT_ROOT/../docubase-documents/Java/OCP21/OCP21.MD"
    

curl -X POST http://localhost:8080/api/question-sets/8bde044c-0199-1000-c76c-000089860bf7/questions \
    -H "Content-Type: multipart/form-data" \
    -F "testid=1" \
    -F "filecomment=This is an text file" \
    -F "file=@/$PROJECT_ROOT/../docubase-documents/Java/OCP21/OCP21.MD"

# authorize as super admin
curl -i -X POST http://localhost:8080/api/authorizations \
    -H "Content-Type: application/json" \
    -d '{"username":"admin1","password":"admin"}'

# authenticate as super admin
curl -i -X POST http://localhost:8080/api/authentications/sign-in \
    -H "Content-Type: application/json" \
    -d '{"username":"super_admin","password":"super_admin"}'

curl -i -X POST http://localhost:8080/api/authentications/access-token \
    -H "Content-Type: application/json" \
    -H "Cookie: refreshable_token=f89aadc001991000218e0000adfb9d9df89aadc0019910007f1700001e50cbc6" \
    -d ''
```

## Run the app

```sh
# reset path to dynamic lib, origin means path to the executable
patchelf --set-rpath '$ORIGIN' ./quiz-flow-0.0.1.kexe
# or
LD_LIBRARY_PATH=".:$LD_LIBRARY_PATH" ./quiz-flow-0.0.1.kexe
```

### Compile Sqlite (outdated)

```sh
# 1. download sqlite-amalgamation-3500400.zip from https://www.sqlite.org/download.html

gcc -fPIC -shared -o libsqlite3.so sqlite3.c -DSQLITE_OMIT_LOAD_EXTENSION -DSQLITE_THREADSAFE=0

gcc -fPIC -shared -o libsqlite3.so sqlite3.c
```

package com.iv127.quizflow.server.acceptance.test.rest.impl

import com.iv127.quizflow.core.rest.api.authentication.AccessTokenResponse
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes
import com.iv127.quizflow.core.rest.api.authentication.AuthenticationsRoutes.Companion.ROUTE_PATH
import com.iv127.quizflow.core.rest.api.authentication.UsernamePasswordAuthenticationRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieRequest
import com.iv127.quizflow.core.rest.api.cookie.CookieResponse
import com.iv127.quizflow.server.acceptance.test.GlobalConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
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
        class CustomCookieStorage : CookiesStorage {
            private val cookies = mutableListOf<Cookie>()

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
    private val cookieAwareHttpClient = GlobalConfig.createConfiguredHttpClient {
        install(HttpCookies) {
            storage = customCookieStorage
        }
    }

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
        val response: HttpResponse = config.performRequest(cookieAwareHttpClient) { client ->
            client.post("${config.baseUrl}/api${ROUTE_PATH}/access-token") {
                contentType(ContentType.Application.Json)
                setBody(mapOf<String, String>())
            }
        }
        val responseData: AccessTokenResponse = response.body<AccessTokenResponse>()
        return responseData
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
