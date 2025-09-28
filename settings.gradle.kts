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

includeModule("webapp")
includeModule("webapp-ui")
includeModule("core")
includeModule("rest")
includeModule("api-automation-tests")

fun path(vararg parts: String): String =
    parts.joinToString(File.separator)

fun includeModule(moduleName: String, vararg pathParts: String) {
    val normalizedPath = if (pathParts.isEmpty()) path(moduleName) else path(*pathParts)
    include(moduleName)
    project(":$moduleName").projectDir = file(normalizedPath)
}
