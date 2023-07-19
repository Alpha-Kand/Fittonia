import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
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
                implementation(compose.desktop.currentOs)
            }
        }
    }
}
compose.desktop {
    application {
        mainClass = "Main_terminalKt" // "Main_desktopKt" "Main_terminalKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.AppImage
            )
            packageName = "FittoniaTerminal" // "FittoniaDesktop" "FittoniaTerminal"
            packageVersion = "1.0"
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = libs.versions.javaVersion.get()
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
}