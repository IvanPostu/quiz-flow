package com.iv127.quizflow.server.acceptance.test.route

import com.iv127.quizflow.core.rest.api.user.UserCreateRequest
import com.iv127.quizflow.core.rest.api.user.UsersRoutes
import com.iv127.quizflow.server.acceptance.test.acceptance.AuthenticationAcceptance
import com.iv127.quizflow.server.acceptance.test.rest.RestErrorException
import com.iv127.quizflow.server.acceptance.test.rest.impl.UsersRoutesTestImpl
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UsersRoutesTest {

    private val userRoutes: UsersRoutes = UsersRoutesTestImpl()

    @Test
    fun testCreateAndGetUserFromList() = runTest {
        val auth = AuthenticationAcceptance.authenticateSuperUser()

        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        val createdUser = userRoutes.create(auth.accessToken, UserCreateRequest(username, password))

        assertThat(createdUser.id).isNotBlank()
        assertThat(createdUser.username).isEqualTo(username)

        val userList = userRoutes.list(auth.accessToken)
        assertThat(userList)
            .anySatisfy({
                assertThat(it).isEqualTo(createdUser)
            })
    }

    @Test
    fun testCreateUsingSameUsername() = runTest {
        val auth = AuthenticationAcceptance.authenticateSuperUser()

        val username = "testUsername1${System.currentTimeMillis()}"
        val password = "test1Password${System.currentTimeMillis()}"
        val createdUser = userRoutes.create(auth.accessToken, UserCreateRequest(username, password))

        assertThat(createdUser.id).isNotBlank()
        assertThat(createdUser.username).isEqualTo(username)

        val e = assertThrows<RestErrorException> {
            userRoutes.create(auth.accessToken, UserCreateRequest(username, password))
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
            userRoutes.create(token, UserCreateRequest(username, password))
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
            userRoutes.list(token)
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
        val auth = AuthenticationAcceptance.authenticateSuperUser()

        val username = ""
        val password = "test1Password${System.currentTimeMillis()}"

        val e = assertThrows<RestErrorException> {
            userRoutes.create(auth.accessToken, UserCreateRequest(username, password))
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("invalid_field_value")
            assertThat(it.message).isEqualTo("Field value is invalid")
            assertThat(it.data).isEqualTo(
                mapOf<String, String?>(
                    "fieldName" to "username",
                    "fieldValue" to "",
                    "message" to "Empty value is not allowed",
                )
            )
        })
    }

    @Test
    fun testCreateWithEmptyPassword() = runTest {
        val auth = AuthenticationAcceptance.authenticateSuperUser()

        val username = "testUsername${System.currentTimeMillis()}"
        val password = ""

        val e = assertThrows<RestErrorException> {
            userRoutes.create(auth.accessToken, UserCreateRequest(username, password))
        }
        assertThat(e.httpStatusCode).isEqualTo(400)
        assertThat(e.restErrorResponse).satisfies({
            assertThat(it.uniqueId).isNotBlank()
            assertThat(it.errorCode).isEqualTo("invalid_field_value")
            assertThat(it.message).isEqualTo("Field value is invalid")
            assertThat(it.data).isEqualTo(
                mapOf<String, String?>(
                    "fieldName" to "password",
                    "fieldValue" to "***",
                    "message" to "Empty value is not allowed",
                )
            )
        })
    }

}
