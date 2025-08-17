plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
    linuxX64()
    macosX64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val linuxX64Main by getting {
            dependencies {
            }
        }
        val macosX64Main by getting {
            dependencies {
            }
        }
        val mingwX64Main by getting {
            dependencies {
            }
        }
    }
}
