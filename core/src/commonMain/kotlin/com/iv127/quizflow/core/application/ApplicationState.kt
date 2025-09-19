package com.iv127.quizflow.core.application

import io.ktor.events.EventDefinition
import io.ktor.server.application.ApplicationModulesLoaded
import io.ktor.server.application.ApplicationModulesLoading
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.ApplicationStarting
import io.ktor.server.application.ApplicationStopPreparing
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.ServerReady

private val statesByEvents = mapOf(
    ApplicationStarting to ApplicationState.APPLICATION_STARTING,
    ApplicationModulesLoading to ApplicationState.APPLICATION_MODULES_LOADING,
    ApplicationModulesLoaded to ApplicationState.APPLICATION_MODULES_LOADED,
    ApplicationStarted to ApplicationState.APPLICATION_STARTED,
    ServerReady to ApplicationState.SERVER_READY,
    ApplicationStopPreparing to ApplicationState.APPLICATION_STOP_PREPARING,
    ApplicationStopping to ApplicationState.APPLICATION_STOPPING,
    ApplicationStopped to ApplicationState.APPLICATION_STOPPED
)

enum class ApplicationState(val event: EventDefinition<out Any>) {
    APPLICATION_STARTING(ApplicationStarting),
    APPLICATION_MODULES_LOADING(ApplicationModulesLoading),
    APPLICATION_MODULES_LOADED(ApplicationModulesLoaded),
    APPLICATION_STARTED(ApplicationStarted),
    SERVER_READY(ServerReady),
    APPLICATION_STOP_PREPARING(ApplicationStopPreparing),
    APPLICATION_STOPPING(ApplicationStopping),
    APPLICATION_STOPPED(ApplicationStopped);

    companion object {
        fun getStateByEvent(event: EventDefinition<out Any>): ApplicationState =
            statesByEvents[event]!!
    }

}
