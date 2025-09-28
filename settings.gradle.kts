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

includeModule("quiz-flow-server")
includeModule("entrypoint", "quiz-flow-server")
includeModule("core", "quiz-flow-server")
includeModule("rest", "quiz-flow-server")

includeModule("webapp-ui")
includeModule("api-automation-tests")

private fun includeModule(moduleName: String, vararg pathParts: String) {
    val mergedParts = buildList {
        addAll(pathParts)
        add(moduleName)
    }
    include(moduleName)
    val projectInstance = project(":$moduleName")
    projectInstance.projectDir = file(mergedParts.joinToString(File.separator))
    projectInstance.name = mergedParts.joinToString("-")
}
