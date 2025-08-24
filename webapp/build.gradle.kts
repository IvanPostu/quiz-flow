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
    jvm()

    tasks {
        val thePackageTask = register("package", Copy::class) {
            group = "package"
            description = "Copies the release exe and resources into one directory"

            from("$buildDir/processedResources/native/main") {
                include("**/*")
            }

            from("$buildDir/bin/native/releaseExecutable") {
                include("**/*")
            }

            into("$buildDir/packaged")
            includeEmptyDirs = false
            dependsOn("nativeProcessResources")
            dependsOn("assemble")
        }

        val zipTask = register<Zip>("packageToZip") {
            group = "package"
            description = "Copies the release exe and resources into one ZIP file."

            archiveFileName.set("packaged.zip")
            destinationDirectory.set(file("$buildDir/packagedZip"))

            from("$buildDir/packaged")

            dependsOn(thePackageTask)
        }
        named("build").get().dependsOn(zipTask.get())

        val runPackaged = register<Exec>("runPackaged") {
            group = "package"
            description = "Run the exe file in the \"packaged\" directory."

            workingDir = File("$buildDir/packaged")

            dependsOn(thePackageTask)
        }
    }

    nativeTarget.apply {
        binaries {
            executable(listOf(RELEASE)) {
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
