package com.iv127.quizflow.server.acceptance.test.route

import com.iv127.quizflow.core.rest.api.authorization.UsernamePasswordAuthorizationRequest
import com.iv127.quizflow.core.rest.api.user.UserCreateRequest
import com.iv127.quizflow.core.rest.api.user.UsersRoutes
import com.iv127.quizflow.server.acceptance.test.rest.RestErrorException
import com.iv127.quizflow.server.acceptance.test.rest.impl.AuthorizationRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.impl.UsersRoutesTestImpl
import com.iv127.quizflow.server.acceptance.test.rest.security.ApiAuthorizationTestImpl
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UsersRoutesTest {

    companion object {
        private val USER_ROUTES: UsersRoutes = UsersRoutesTestImpl()
        private val AUTHORIZATION_ROUTES = AuthorizationRoutesTestImpl()
    }

    @Test
    fun testCreateAndGetUserFromList() = runTest {
        val auth = AUTHORIZATION_ROUTES.authorize(UsernamePasswordAuthorizationRequest("super_admin", "super_admin"))

        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        val createdUser = USER_ROUTES.create(ApiAuthorizationTestImpl(auth), UserCreateRequest(username, password))

        assertThat(createdUser.id).isNotBlank()
        assertThat(createdUser.username).isEqualTo(username)

        val userList = USER_ROUTES.list(ApiAuthorizationTestImpl(auth))
        assertThat(userList)
            .anySatisfy({
                assertThat(it).isEqualTo(createdUser)
            })
    }

    @Test
    fun testCreateUsingSameUsername() = runTest {
        val auth = AUTHORIZATION_ROUTES.authorize(UsernamePasswordAuthorizationRequest("super_admin", "super_admin"))

        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        val createdUser = USER_ROUTES.create(ApiAuthorizationTestImpl(auth), UserCreateRequest(username, password))

        assertThat(createdUser.id).isNotBlank()
        assertThat(createdUser.username).isEqualTo(username)

        val e = assertThrows<RestErrorException> {
            USER_ROUTES.create(ApiAuthorizationTestImpl(auth), UserCreateRequest(username, password))
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("username_was_already_taken")
            assertThat(it.message).isEqualTo("An user with such username already exists")
            assertThat(it.data).isEqualTo(mapOf<String, String>())
        })
    }

    @Test
    fun testCreateUserWithRandomStringAsToken() = runTest {
        val token = "token${System.currentTimeMillis()}"
        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        val e = assertThrows<RestErrorException> {
            USER_ROUTES.create(ApiAuthorizationTestImpl(token), UserCreateRequest(username, password))
        }
        assertThat(e.httpStatusCode).isEqualTo(401)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("authentication_error")
            assertThat(it.message).isEqualTo("Authentication failed")
            assertThat(it.data).isEqualTo(
                mapOf(
                    "reason" to "Access token is invalid"
                )
            )
        })
    }

    @Test
    fun testListUsersWithRandomStringAsToken() = runTest {
        val token = "token${System.currentTimeMillis()}"
        val e = assertThrows<RestErrorException> {
            USER_ROUTES.list(ApiAuthorizationTestImpl(token))
        }
        assertThat(e.httpStatusCode).isEqualTo(401)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("authentication_error")
            assertThat(it.message).isEqualTo("Authentication failed")
            assertThat(it.data).isEqualTo(
                mapOf(
                    "reason" to "Access token is invalid"
                )
            )
        })
    }

    @Test
    fun testCreateWithEmptyUsername() = runTest {
        val auth = AUTHORIZATION_ROUTES.authorize(UsernamePasswordAuthorizationRequest("super_admin", "super_admin"))

        val username = ""
        val password = "test1Password${System.currentTimeMillis()}"

        val e = assertThrows<RestErrorException> {
            USER_ROUTES.create(ApiAuthorizationTestImpl(auth), UserCreateRequest(username, password))
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("invalid_username")
            assertThat(it.message).isEqualTo("Username is invalid")
            assertThat(it.data).isEqualTo(mapOf<String, String>())
        })
    }

    @Test
    fun testCreateWithEmptyPassword() = runTest {
        val auth = AUTHORIZATION_ROUTES.authorize(UsernamePasswordAuthorizationRequest("super_admin", "super_admin"))

        val username = "testUsername${System.currentTimeMillis()}"
        val password = ""

        val e = assertThrows<RestErrorException> {
            USER_ROUTES.create(ApiAuthorizationTestImpl(auth), UserCreateRequest(username, password))
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("invalid_password")
            assertThat(it.message).isEqualTo("Password is invalid")
            assertThat(it.data).isEqualTo(mapOf<String, String>())
        })
    }

}
