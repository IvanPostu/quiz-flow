plugins {
    id("java")
    kotlin("jvm")
}

group = "com.iv127.quizflow.api.automation.tests"
version = "1.0-SNAPSHOT"

sourceSets {
    main {
        runtimeClasspath += sourceSets["test"].output
    }
}
configurations {
    runtimeClasspath {
        extendsFrom(configurations.testImplementation.get(), configurations.testRuntimeOnly.get())
    }
}

// This project is not part of the CI and shouldn't be run on build.
// It should be run explicitly from the command line: `java -jar ....jar`,
// the custom config below is a hack that enables test task only if --tests flag is present.
tasks.test {
    onlyIf {
        val hasTestsArg = gradle.startParameter.taskRequests.any { request ->
            request.args.any { it.startsWith("--tests") }
        }

        if (!hasTestsArg) {
            logger.lifecycle("Skipping tests: No --tests argument provided.")
        }

        hasTestsArg
    }
}

dependencies {
    implementation("org.junit.platform:junit-platform-launcher:1.13.4")

    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.cio)
    testImplementation(libs.ktor.client.content.negotiation)

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}
