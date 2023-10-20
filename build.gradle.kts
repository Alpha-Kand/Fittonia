import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    alias(libs.plugins.android.application) // False positive error.
}

version = "1.0"

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}
kotlin {
    android()
    jvm("desktop")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.runtime)
                implementation(libs.kotlinx.coroutines.core.library)
                implementation(libs.jackson.module.kotlin)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                api(libs.mockk.library)
                api(libs.junit.jupiter.api)
                api(libs.junit.jupiter.params)
                api(libs.junit.jupiter.engine)
                implementation(libs.junit)
                api(libs.junit.jupiter)
                api(libs.kotlinx.coroutines.test)
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)
            kotlin.srcDirs("src/jvmMain/kotlin")
            dependencies {
                implementation(libs.appcompat.library)
                implementation(libs.activity.compose.library)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core.library)
                implementation(compose.desktop.currentOs)
                implementation(libs.kotter.library)
            }
        }
    }
}
compose.desktop {
    application {
        val buildType = Properties().run {
            load(file("local.properties").inputStream())
            getProperty("DESKTOP_BUILD_TYPE", "")
        }.toInt()

        mainClass = when (buildType) {
            1 -> "Main_terminalKt"
            2 -> "Main_clientengineKt"
            3 -> "Main_desktopKt"
            else -> "???"
        }

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.AppImage,
            )
            packageName = when (buildType) {
                1 -> "FittoniaTerminal"
                2 -> "FittoniaClientEngine"
                3 -> "FittoniaDesktop"
                else -> "???"
            }
            packageVersion = "1.0"
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = libs.versions.javaVersion.get()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res", "src/commonMain/resources")
        }
    }
    namespace = "org.huntersmeadow.fittonia"
}

dependencies {
    api(libs.mockk.library)
    api(libs.junit.jupiter.api)
    api(libs.junit.jupiter.params)
    api(libs.junit.jupiter.engine)
    api(libs.junit.vintage.engine)
    implementation(libs.junit)
    api(libs.junit.jupiter)
}
