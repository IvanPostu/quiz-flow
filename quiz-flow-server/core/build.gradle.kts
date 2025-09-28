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
                val bcryptDefFile = file("src/nativeInterop/cinterop/bcrypt.def")
                bcryptDefFile.writeText(
                    """
                        compilerOpts = -I $parentProjectAbsolutePath/native/bcrypt
                        headers = bcrypt.h
                        linkerOpts = -L $parentProjectAbsolutePath/native/bcrypt -l:libbcrypt.so.1
                    """.trimIndent()
                )
                cinterops {
                    val libsimple by creating
                    val sqlite3 by creating
                    val bcrypt by creating
                }
            }
        }
        val commonMain by getting {
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
            dependencies {
                implementation(projects.quizFlowServerRest)
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.server.cio)
                implementation(libs.markdown)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.status.pages)
                implementation(libs.koin.core)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.0")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.sqlite.jdbc)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.assertj)
                implementation(libs.slf4j.simple)
            }
        }
        val linuxX64Test by getting {
        }
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}
