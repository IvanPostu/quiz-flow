package com.iv127.quizflow.core.services.authorization

import com.iv127.quizflow.core.model.User
import com.iv127.quizflow.core.model.authorization.AccessDeniedException
import com.iv127.quizflow.core.model.authorization.Authorization
import com.iv127.quizflow.core.model.authorization.AuthorizationBuilder
import com.iv127.quizflow.core.model.authorization.AuthorizationNotFoundException

interface AuthorizationService {

    fun create(user: User, originAuthorization: Authorization?): Authorization

    @Throws(AccessDeniedException::class, AuthorizationNotFoundException::class)
    fun updateById(
        authorization: Authorization,
        authorizationId: String,
        updateFunc: (authorizationBuilder: AuthorizationBuilder) -> Unit
    ): Authorization

    @Throws(AuthorizationNotFoundException::class)
    fun getByAccessToken(accessToken: String): Authorization

}
