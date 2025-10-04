package com.iv127.quizflow.core.ktor

import com.iv127.quizflow.core.model.authorization.Authorization
import com.iv127.quizflow.core.rest.api.authorization.AnonymousAuthorization
import com.iv127.quizflow.core.rest.api.authorization.ApiAuthorization
import com.iv127.quizflow.core.services.authorization.AuthorizationService
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.call
import io.ktor.util.AttributeKey
import org.koin.core.KoinApplication

class AuthorizationPlugin(koinApp: KoinApplication) : BaseApplicationPlugin<ApplicationCallPipeline, Unit, Unit> {
    companion object {
        val AUTHORIZATION_KEY = AttributeKey<ApiAuthorization>("Authorization")
    }

    private val authorizationService: AuthorizationService by koinApp.koin.inject()

    override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit) {
        pipeline.intercept(ApplicationCallPipeline.Plugins) {
            val authHeader = call.request.headers["Authorization"]
            val bearerToken = authHeader?.removePrefix("Bearer ")?.trim()

            if (bearerToken.isNullOrBlank()) {
                call.attributes.put(AUTHORIZATION_KEY, AnonymousAuthorization)
                return@intercept
            }
            val authorization: Authorization? = authorizationService.getByAccessToken(bearerToken)
            if (authorization == null) {
                call.attributes.put(AUTHORIZATION_KEY, AnonymousAuthorization)
                return@intercept
            }
            call.attributes.put(AUTHORIZATION_KEY, authorization)
        }
    }

    override val key: AttributeKey<Unit> = AttributeKey("AuthorizationPlugin")
}
