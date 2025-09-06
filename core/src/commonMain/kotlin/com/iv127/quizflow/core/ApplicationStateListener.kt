package com.iv127.quizflow.core

import com.iv127.quizflow.core.utils.getClassFullName
import io.ktor.events.EventDefinition
import io.ktor.util.logging.KtorSimpleLogger
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ApplicationStateListener {

    private val logger = KtorSimpleLogger(getClassFullName(Application::class))
    private val states: MutableList<EventDefinition<out Any>> = mutableListOf()
    private val statesEventTimesMillis: MutableList<Long> = mutableListOf()

    @OptIn(ExperimentalTime::class)
    fun transition(state: EventDefinition<out Any>, nameProvider: (state: EventDefinition<out Any>) -> String) {
        val now = Clock.System.now().toEpochMilliseconds()
        val prevState = if (states.isEmpty()) null else states.last();
        if (prevState != null) {
            val prevStateEventTimesMillis = statesEventTimesMillis.last()
            logger.info(
                "Application state transition changed from: ${nameProvider(prevState)} to: ${
                    nameProvider(state)
                }. Time taken: ${now - prevStateEventTimesMillis} millis"
            )
        } else {
            logger.info(
                "Application initial state is set to: ${nameProvider(state)}"
            )
        }

        states.add(state)
        statesEventTimesMillis.add(now)
    }

}
