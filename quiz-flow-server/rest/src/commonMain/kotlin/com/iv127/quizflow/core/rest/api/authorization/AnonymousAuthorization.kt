package com.iv127.quizflow.core.rest.api.authorization

data class AnonymousAuthorization(val message: String) : ApiAuthorization {

    override fun getToken(): String {
        return ""
    }

}
