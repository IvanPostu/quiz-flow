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

// This project is not part of the CI and shouldn't be run on build
// It should be run explicitly from the command line: `java -jar ....jar`
tasks.named<Test>("test") {
    enabled = false
}


dependencies {
    implementation("org.junit.platform:junit-platform-launcher:1.13.4")

    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}
