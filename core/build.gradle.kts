val ktor_version = "3.2.3"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinPluginSerialization)
}

kotlin {
    val parentProjectAbsolutePath = rootProject.projectDir.absolutePath
    val hostOs = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && arch == "x86_64" -> macosX64()
        hostOs == "Mac OS X" && arch == "aarch64" -> macosArm64()
        hostOs == "Linux" -> linuxX64()
        isMingwX64 -> mingwX64()
        // Other supported targets are listed here: https://ktor.io/docs/native-server.html#targets
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    jvm()

    sourceSets {
        nativeTarget.apply {
            compilations.getByName("main") {
                val libsimpleDefFile = file("src/nativeInterop/cinterop/libsimple.def")
                libsimpleDefFile.writeText(
                    """
                        compilerOpts = -I $parentProjectAbsolutePath/native/simple
                        headers = simple.h
                        
                        linkerOpts = -L $parentProjectAbsolutePath/native/simple -lsimple
                    """.trimIndent()
                )
                val sqliteDefFile = file("src/nativeInterop/cinterop/sqlite3.def")
                sqliteDefFile.writeText(
                    """
                        compilerOpts = -I $parentProjectAbsolutePath/native/sqlite-amalgamation-3500400
                        headers = sqlite3.h
                        
                        linkerOpts = -L $parentProjectAbsolutePath/native/sqlite-amalgamation-3500400 -lsqlite3 --allow-shlib-undefined
                        excludedFunctions = sqlite3_mutex_held \
                          sqlite3_mutex_notheld \
                          sqlite3_snapshot_cmp \
                          sqlite3_snapshot_free \
                          sqlite3_snapshot_get \
                          sqlite3_snapshot_open \
                          sqlite3_snapshot_recover \
                          sqlite3_set_last_insert_rowid \
                          sqlite3_stmt_scanstatus \
                          sqlite3_stmt_scanstatus_reset \
                          sqlite3_column_database_name \
                          sqlite3_column_database_name16 \
                          sqlite3_column_origin_name \
                          sqlite3_column_origin_name16 \
                          sqlite3_column_table_name \
                          sqlite3_column_table_name16 \
                          sqlite3_enable_load_extension \
                          sqlite3_load_extension \
                          sqlite3_unlock_notify
                    """.trimIndent()
                )
                cinterops {
                    val libsimple by creating
                    val sqlite3 by creating
                }
            }
        }
        val commonMain by getting {
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
                implementation("org.jetbrains:markdown:0.7.3")
                implementation("io.ktor:ktor-server-core:$ktor_version")
                implementation("io.ktor:ktor-server-cio:$ktor_version")
                implementation("io.ktor:ktor-server-status-pages:$ktor_version")
                implementation(libs.koin.core)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.xerial:sqlite-jdbc:3.49.1.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.assertj)
                implementation("org.slf4j:slf4j-simple:2.0.17")
            }
        }
        val linuxX64Test by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}
