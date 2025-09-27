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

// This project is not part of the CI and test shouldn't be run on build.
// It should be run explicitly from the command line: `java -jar ....jar`,
tasks.getByName<Test>("test") {
    enabled = false
}

tasks.register<Test>("testMain") {
    description = "Runs tests under main"
    group = "verification"

    useJUnitPlatform() // or useTestNG(), useJUnit(), etc.

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    filter {
        includeTestsMatching("com.iv127.*")
    }

    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = false
    }

    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {}

        override fun beforeTest(testDescriptor: TestDescriptor) {}

        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
            println("Test '${testDescriptor.className}.${testDescriptor.name}' ${result.resultType}")
        }
    })
}

dependencies {
    implementation("org.junit.platform:junit-platform-launcher:1.13.4")
    implementation(projects.rest)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)

    implementation(libs.slf4j.simple)

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation(libs.assertj)
}
