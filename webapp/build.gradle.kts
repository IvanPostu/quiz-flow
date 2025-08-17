val ktor_version = "3.2.3"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val nativeTarget = when {
        hostOs == "Mac OS X" && arch == "x86_64" -> macosX64("native")
        hostOs == "Mac OS X" && arch == "aarch64" -> macosArm64("native")
        hostOs == "Linux" -> linuxX64("native")
        hostOs == "Windows" -> mingwX64("native")
        // Other supported targets are listed here: https://ktor.io/docs/native-server.html#targets
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    jvm()

    nativeTarget.apply {
        binaries {
            executable(rootProject.name, listOf(RELEASE)) {
                entryPoint = "com.iv127.quizpool.webapp.main"
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
