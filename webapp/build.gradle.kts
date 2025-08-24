import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

val ktor_version = "3.2.3"

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

    jvm()

    tasks {
        val thePackageTask = register("package", Copy::class) {
            group = "package"
            description = "Copies the ${buildType.toString().lowercase()} exe and resources into one directory"

            from("${layout.buildDirectory}/processedResources/native/main") {
                include("**/*")
            }

            from("${layout.buildDirectory}/bin/native/${buildType.toString().lowercase()}Executable") {
                include("**/*")
            }

            into("${layout.buildDirectory}/packaged")
            includeEmptyDirs = false
            dependsOn("nativeProcessResources")
            dependsOn("assemble")
        }

        val zipTask = register<Zip>("packageToZip") {
            group = "package"
            description = "Copies the ${buildType.toString().lowercase()} exe and resources into one ZIP file."

            archiveFileName.set("packaged.zip")
            destinationDirectory.set(file("${layout.buildDirectory}/packagedZip"))

            from("${layout.buildDirectory}/packaged")

            dependsOn(thePackageTask)
        }
        named("build").get().dependsOn(zipTask.get())

        register<Exec>("runPackaged") {
            group = "package"
            description = "Run the exe file in the \"packaged\" directory."

            workingDir = File("${layout.buildDirectory}/packaged")

            dependsOn(thePackageTask)
        }
    }

    nativeTarget.apply {
        binaries {
            executable(listOf(buildType)) {
                entryPoint = "com.iv127.quizflow.webapp.main"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.core)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.slf4j:slf4j-simple:2.0.17")
            }
        }
        val nativeMain by getting {
            dependencies {

            }
        }
        val nativeTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-server-test-host:$ktor_version")
            }
        }
    }
}
