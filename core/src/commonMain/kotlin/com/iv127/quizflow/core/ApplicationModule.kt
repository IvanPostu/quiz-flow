package com.iv127.quizflow.core

import com.iv127.quizflow.core.platform.PlatformServices
import com.iv127.quizflow.core.rest.routes.HealthCheckRoutes
import com.iv127.quizflow.core.rest.routes.QuizRoutes
import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.ApplicationModulesLoaded
import io.ktor.server.application.ApplicationModulesLoading
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStarting
import io.ktor.server.application.ApplicationStopPreparing
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.ServerReady
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.Logger
import kotlin.reflect.KClass
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.logger.MESSAGE
import org.koin.core.parameter.ParametersHolder
import org.koin.dsl.module
import org.koin.dsl.onClose

private val applicationStates = mapOf(
    ApplicationStarting to "ApplicationStarting",
    ApplicationModulesLoading to "ApplicationModulesLoading",
    ApplicationModulesLoaded to "ApplicationModulesLoaded",
    ApplicationStarted to "ApplicationStarted",
    ServerReady to "ServerReady",
    ApplicationStopPreparing to "ApplicationStopPreparing",
    ApplicationStopping to "ApplicationStopping",
    ApplicationStopped to "ApplicationStopped"
)

fun createApplicationModule(platformServices: PlatformServices): Application.() -> Unit {
    val koinLogger = setupKoinLogger()
    val appModule = module {
        single { platformServices }.onClose {
            platformServices.close()
            koinLogger.info("Platform services have successfully closed")
        }
        factory { (clazz: KClass<*>) -> KtorSimpleLogger(getClassFullName(clazz)) }
    }
    val koinApp = startKoin {
        modules(appModule)
        logger(koinLogger)
    }
    val log: Logger = koinApp.koin.get(Logger::class, parameters = {
        ParametersHolder(mutableListOf(ApplicationModule::class))
    })

    val routeInstances = listOf(
        HealthCheckRoutes(),
        QuizRoutes(),
    )
    val processUtils = platformServices.getProcessUtils()
    val fileIo = platformServices.getFileIO()
    val pathToPublicDirectory = processUtils.getPathToExecutableDirectory() + "public"
    val staticFilesProviderPlugin = StaticFilesProvider(fileIo, "/public", pathToPublicDirectory)
    val requestTracePlugin = createRouteScopedPlugin("RequestTracePlugin", { }) {
        onCall { call ->
            log.info("${call.request.httpMethod}: ${call.request.uri}")
        }
    }

    val stateListener = ApplicationStateListener()
    return {
        for (event in applicationStates) {
            this.monitor.subscribe(event.key) {
                stateListener.transition(event.key, {
                    applicationStates[it]!!
                })
            }
        }
        this.monitor.subscribe(ApplicationStopping) {
            koinApp.close()
        }
        install(requestTracePlugin)
        intercept(ApplicationCallPipeline.Call) {
            staticFilesProviderPlugin.intercept(this)
        }

        // TODO: enable only for development
        install(StatusPages) {
            exception<Throwable> { call, exception ->
                log.error("Unhandled exception: ", exception)
                val stackTrace = exception.stackTraceToString()

                call.respond(
                    """
                        |Error: ${exception.message}
                        |Stack Trace:
                        |$stackTrace
                    """.trimMargin(),
                    typeInfo = null
                )
            }
        }

        routing {
            route("/api") {
                routeInstances.forEach { it.setup(this) }
            }
            get("/") {
                call.respondBytes(staticFilesProviderPlugin.getIndexHtmlStaticFileOrElse("Index html is missing!"))
            }
            route("/{...}") {
                handle {
                    val requestUri = call.request.uri
                    val match = Regex("^/(?<path>(?!api|test).*)$").find(requestUri)
                    if (match == null) {
                        call.respond(HttpStatusCode.NotFound, typeInfo = null)
                    } else {
                        call.respondRedirect("/", permanent = false)
                    }
                }
            }
        }
    }
}

private fun setupKoinLogger(): org.koin.core.logger.Logger {
    val logger = KtorSimpleLogger(getClassFullName(KoinApplication::class))
    return object : org.koin.core.logger.Logger() {
        override fun display(level: Level, msg: MESSAGE) {
            if (level == Level.DEBUG) {
                logger.debug(msg)
            }
            if (level == Level.INFO) {
                logger.info(msg)
            }
            if (level == Level.WARNING) {
                logger.warn(msg)
            }
            if (level == Level.ERROR) {
                logger.error(msg)
            }
        }
    }
}

class ApplicationModule
