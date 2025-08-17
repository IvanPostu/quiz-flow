plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val nativeTarget = when {
        hostOs == "Mac OS X" && arch == "x86_64" -> macosX64()
        hostOs == "Mac OS X" && arch == "aarch64" -> macosArm64()
        hostOs == "Linux" -> linuxX64()
        hostOs == "Windows" -> mingwX64()
        // Other supported targets are listed here: https://ktor.io/docs/native-server.html#targets
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains:markdown:0.7.3")
            }
        }
    }
}
