import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && arch == "x86_64" -> macosX64("native")
        hostOs == "Mac OS X" && arch == "aarch64" -> macosArm64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        // Other supported targets are listed here: https://ktor.io/docs/native-server.html#targets
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    val buildType: NativeBuildType = if (project.hasProperty("buildType")) NativeBuildType.valueOf(
        project.properties.get("buildType").toString()
    ) else NativeBuildType.DEBUG
    val outputDir = layout.buildDirectory.get().asFile
    val targetBaseName = "quiz-flow-${project.version}"

    jvm()

    tasks.named<Jar>("jvmJar") {
        dependsOn("jvmMainClasses")

        archiveClassifier.set("runnable")
        from(kotlin.targets["jvm"].compilations["main"].output)

        manifest {
            attributes(
                "Main-Class" to "com.iv127.quizflow.entrypoint.MainKt"
            )
        }

        exclude("META-INF/versions/9/module-info.class")
        exclude("META-INF/LICENSE.txt")

        from({
            configurations["jvmRuntimeClasspath"].filter { it.name.endsWith("jar") }.map { zipTree(it) }
        })
        archiveFileName.set("${targetBaseName}.jar")
    }

    tasks {
        val customProcessResources = register("customProcessResources", Copy::class) {
            from("${outputDir}/processedResources/native/main") {
                include("**/*")
                into("resources")
            }
            from(project(":quiz-flow-server-core").file("src/commonMain/resources")) {
                include("**/*")
                exclude("public") // exclude public folder
                into("resources")
            }
            from(project(":webapp-ui").file("dist")) {
                include("**/*")
            }

            destinationDir = file("${outputDir}/packaged")
            includeEmptyDirs = false

            doLast {
                mkdir("$destinationDir/db")
            }

            dependsOn("nativeProcessResources")
            dependsOn("assemble")
        }
        val thePackageTask = register("package", Copy::class) {
            this.group = "package"
            this.description = "Copies the ${buildType.toString().lowercase()} exe and resources into one directory"

            from("${outputDir}/bin/native/${buildType.toString().lowercase()}Executable") {
                include("**/*")
            }
            from("${outputDir}/libs") {
                include("${targetBaseName}.jar")
            }

            destinationDir = file("${outputDir}/packaged")
            includeEmptyDirs = false
            dependsOn(customProcessResources)
        }
        val zipTask = register<Zip>("packageToZip") {
            group = "package"
            description = "Copies the ${buildType.toString().lowercase()} exe and resources into one ZIP file."

            archiveFileName.set("packaged.zip")
            destinationDirectory.set(file("${outputDir}/packagedZip"))

            from("${outputDir}/packaged")

            dependsOn(thePackageTask)
        }
        named("build").get().dependsOn(zipTask.get())
        named("nativeTest").get().dependsOn(customProcessResources.get())
        named("jvmTest").get().dependsOn(customProcessResources.get())
    }

    nativeTarget.apply {
        binaries {
            executable(listOf(buildType)) {
                entryPoint = "com.iv127.quizflow.entrypoint.main"
                baseName = targetBaseName
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.quizFlowServerCore)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.slf4j.simple)
            }
        }
        val nativeMain by getting {
            dependencies {

            }
        }
        val nativeTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.ktor.server.test.host)
            }
        }
    }
}
