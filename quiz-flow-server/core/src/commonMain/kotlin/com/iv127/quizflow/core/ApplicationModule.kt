package com.iv127.quizflow.core

import com.iv127.quizflow.core.application.ApplicationState
import com.iv127.quizflow.core.platform.PlatformServices
import com.iv127.quizflow.core.rest.ApiRoute
import com.iv127.quizflow.core.rest.impl.authorization.AuthorizationRoutesImpl
import com.iv127.quizflow.core.rest.impl.healthcheck.HealthCheckRoutesImpl
import com.iv127.quizflow.core.rest.impl.question.QuestionsRoutesImpl
import com.iv127.quizflow.core.rest.impl.questionset.QuestionSetsRoutesImpl
import com.iv127.quizflow.core.rest.impl.user.UsersRoutesImpl
import com.iv127.quizflow.core.services.authorization.AuthorizationService
import com.iv127.quizflow.core.services.authorization.impl.AuthorizationServiceImpl
import com.iv127.quizflow.core.services.questionset.QuestionSetService
import com.iv127.quizflow.core.services.questionset.impl.QuestionSetServiceImpl
import com.iv127.quizflow.core.services.user.UserService
import com.iv127.quizflow.core.services.user.impl.UserServiceImpl
import com.iv127.quizflow.core.sqlite.SqliteDatabase
import com.iv127.quizflow.core.sqlite.migrator.DatabaseMigrator
import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
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
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose

private const val APP_DB_NAME = "app.db"

fun createApplicationModule(platformServices: PlatformServices): Application.() -> Unit {
    val koinLogger = setupKoinLogger()
    val stateListener = ApplicationStateListener()

    val appModule = module {
        single { platformServices }.onClose {
            platformServices.close()
            koinLogger.info("Platform services have successfully closed")
        }
        single { stateListener }
        factory { (clazz: KClass<*>) -> KtorSimpleLogger(getClassFullName(clazz)) }
        factory(named("appDb")) { getAppDatabase(platformServices) }
        single<QuestionSetService> {
            QuestionSetServiceImpl {
                get<SqliteDatabase>(named("appDb"))
            }
        }
        single<UserService> {
            UserServiceImpl {
                get<SqliteDatabase>(named("appDb"))
            }
        }
        single<AuthorizationService> {
            AuthorizationServiceImpl {
                get<SqliteDatabase>(named("appDb"))
            }
        }
    }
    val koinApp: KoinApplication = startKoin {
        modules(appModule)
        logger(koinLogger)
    }
    val log: Logger = koinApp.koin.get(Logger::class, parameters = {
        ParametersHolder(mutableListOf(ApplicationModule::class))
    })
    checkAndLogIfDebugApplicationRootFolderEnvVariableWasSet(platformServices, log)
    checkIfDatabaseIsAccessible(koinApp)

    val routeInstances: List<ApiRoute> = listOf(
        HealthCheckRoutesImpl(koinApp),
        QuestionSetsRoutesImpl(koinApp),
        UsersRoutesImpl(koinApp),
        QuestionsRoutesImpl(koinApp),
        AuthorizationRoutesImpl(koinApp),
    )
    val processUtils = platformServices.getProcessUtils()
    val fileIo = platformServices.getFileIO()
    val pathToPublicDirectory = processUtils.getPathToExecutableDirectory() + fileIo.getPathSeparator() + "public"
    val staticFilesProviderPlugin = StaticFilesProvider(fileIo, "/public", pathToPublicDirectory)
    val requestTracePlugin = createRouteScopedPlugin("RequestTracePlugin", { }) {
        onCall { call ->
            log.info("${call.request.httpMethod}: ${call.request.uri}")
        }
    }

    return {
        for (entry in ApplicationState.entries) {
            this.monitor.subscribe(entry.event) {
                stateListener.transition(entry.event)
            }
        }
        this.monitor.subscribe(ApplicationStopping) {
            koinApp.close()
        }
        install(ContentNegotiation) {
            json() // uses kotlinx.serialization.json.Json by default
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
                for (routeInstance in routeInstances) {
                    routeInstance.setup(this)
                }
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

private fun checkAndLogIfDebugApplicationRootFolderEnvVariableWasSet(platformServices: PlatformServices, log: Logger) {
    val debugApplicationRootFolder = platformServices.getProcessUtils()
        .runShellScriptAndGetOutput("echo -n \$DEBUG_APPLICATION_ROOT_FOLDER").output
    if (debugApplicationRootFolder.isNotBlank()) {
        log.warn(
            "Env variable DEBUG_APPLICATION_ROOT_FOLDER=${
                debugApplicationRootFolder
            } is returned by getPathToExecutableDirectory(), make sure it is used for development only"
        )
    }
}

private fun checkIfDatabaseIsAccessible(koinApp: KoinApplication) {
    val db = koinApp.koin.get<SqliteDatabase>(named("appDb"))
    if (db.executeAndGetResultSet("SELECT 1 as test;")[0]["test"]!!.toInt() != 1) {
        throw IllegalStateException("appDb accessibility check failed")
    }
}

private fun getAppDatabase(platformServices: PlatformServices): SqliteDatabase {
    val pathToExecutable = platformServices.getProcessUtils().getPathToExecutableDirectory()
    val dbPath = platformServices.getPathUtils().resolve(pathToExecutable, "db", APP_DB_NAME)
    val dbInstance = platformServices
        .getSqliteDatabase(dbPath, DatabaseMigrator(platformServices.getPathUtils(), platformServices.getResource()))
    return dbInstance
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
