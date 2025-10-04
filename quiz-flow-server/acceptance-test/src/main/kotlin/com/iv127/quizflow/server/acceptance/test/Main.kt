package com.iv127.quizflow.server.acceptance.test

import kotlin.system.exitProcess
import org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val listener = SummaryGeneratingListener()

            val request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("com.iv127.quizflow.api.automation.tests"))
                .build()

            val launcher = LauncherFactory.create()
            launcher.registerTestExecutionListeners(listener)
            launcher.execute(request)

            val summary = listener.summary
            println("Tests run: ${summary.testsFoundCount}")
            println("Success: ${summary.testsSucceededCount}")
            println("Failures: ${summary.testsFailedCount}")

            summary.failures.forEach {
                println("${it.testIdentifier.displayName}: ${it.exception}")
            }

            if (summary.failures.isEmpty()) {
                exitProcess(0)
            } else {
                exitProcess(1)
            }
        }
    }
}
