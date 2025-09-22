rootProject.name = "quiz-flow"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":webapp")
include(":webapp-ui")
include(":core")
include(":api-automation-tests")
