import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

val ktor_version = "3.2.3"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    val parentProjectAbsolutePath = project.parent?.file(".")?.absolutePath
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
                "Main-Class" to "com.iv127.quizflow.webapp.MainKt"
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
        val thePackageTask = register("package", Copy::class) {
            this.group = "package"
            this.description = "Copies the ${buildType.toString().lowercase()} exe and resources into one directory"

            from("${outputDir}/bin/native/${buildType.toString().lowercase()}Executable") {
                include("**/*")
            }
            from("${outputDir}/libs") {
                include("${targetBaseName}.jar")
            }
            from("${outputDir}/processedResources/native/main") {
                include("**/*")
                into("resources")
            }
            from(project(":core").file("src/commonMain/resources")) {
                include("**/*")
                exclude("public") // exclude public folder
                into("resources")
            }
            from(project(":webapp-ui").file("dist")) {
                include("**/*")
            }

            destinationDir = file("${outputDir}/packaged")
            includeEmptyDirs = false
            dependsOn("nativeProcessResources")
            dependsOn("assemble")
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

        register<Exec>("runPackaged") {
            group = "package"
            description = "Run the exe file in the \"packaged\" directory."

            workingDir = File("${outputDir}/packaged")

            dependsOn(thePackageTask)
        }
    }

    nativeTarget.apply {
        binaries {
            executable(listOf(buildType)) {
                entryPoint = "com.iv127.quizflow.webapp.main"
                baseName = targetBaseName
            }
            compilations.getByName("main") {
                val libsimpleDefFile = file("src/nativeInterop/cinterop/libsimple.def")
                libsimpleDefFile.writeText(
                    """
                        compilerOpts = -I $parentProjectAbsolutePath/native/simple
                        headers = simple.h
                        
                        linkerOpts = -L $parentProjectAbsolutePath/native/simple -l simple
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
