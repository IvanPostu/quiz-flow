package com.iv127.quizflow.core

import com.iv127.quizflow.core.application.ApplicationState
import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.events.EventDefinition
import io.ktor.util.logging.KtorSimpleLogger
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ApplicationStateListener {

    private val logger = KtorSimpleLogger(getClassFullName(Application::class))
    private val states: MutableList<ApplicationState> = mutableListOf()
    private val statesEventTimesMillis: MutableList<Long> = mutableListOf()

    @OptIn(ExperimentalTime::class)
    fun transition(eventDefinition: EventDefinition<out Any>) {
        val appState = ApplicationState.getStateByEvent(eventDefinition)
        val now = Clock.System.now().toEpochMilliseconds()
        val prevState = if (states.isEmpty()) null else states.last();
        if (prevState != null) {
            val prevStateEventTimesMillis = statesEventTimesMillis.last()
            logger.info(
                "Application state transition changed from: ${prevState.name} to: ${
                    appState.name
                }. Time taken: ${now - prevStateEventTimesMillis} millis"
            )
        } else {
            logger.info(
                "Application initial state is set to: ${appState.name}"
            )
        }

        states.add(appState)
        statesEventTimesMillis.add(now)
    }

    fun getActualState(): ApplicationState {
        if (states.isEmpty()) {
            return ApplicationState.APPLICATION_STARTING
        }
        return states.last()
    }

}
