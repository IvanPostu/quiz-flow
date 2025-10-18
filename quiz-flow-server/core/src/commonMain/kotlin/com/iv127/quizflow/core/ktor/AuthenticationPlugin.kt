package com.iv127.quizflow.core.ktor

import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.call
import io.ktor.util.AttributeKey
import org.koin.core.KoinApplication

class AuthenticationPlugin(koinApp: KoinApplication) : BaseApplicationPlugin<ApplicationCallPipeline, Unit, Unit> {
    companion object {
        private const val PLUGIN_NAME: String = "AuthenticationPlugin"
        val AUTHENTICATION_KEY = AttributeKey<String>(PLUGIN_NAME)
    }

    override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit) {
        pipeline.intercept(ApplicationCallPipeline.Plugins) {
            val authHeader = call.request.headers["Authorization"]
            val bearerToken = authHeader?.removePrefix("Bearer ")?.trim()

            if (bearerToken.isNullOrBlank()) {
                call.attributes.put(AUTHENTICATION_KEY, "")
                return@intercept
            }
            call.attributes.put(AUTHENTICATION_KEY, bearerToken)
        }
    }

    override val key: AttributeKey<Unit> = AttributeKey(PLUGIN_NAME)
}
