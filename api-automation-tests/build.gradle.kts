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

dependencies {
    implementation("org.junit.platform:junit-platform-launcher:1.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}
