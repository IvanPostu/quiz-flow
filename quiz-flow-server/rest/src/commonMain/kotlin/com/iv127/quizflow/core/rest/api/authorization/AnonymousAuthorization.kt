package com.iv127.quizflow.core.rest.api.authorization

object AnonymousAuthorization : ApiAuthorization {
    override fun getToken(): String {
        return ""
    }
}
