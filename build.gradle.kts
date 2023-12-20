import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kover)
}

version = "1.0"

repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val testAttribute: Attribute<String> = Attribute.of("key", String::class.java)

kotlin {
    android()
    jvm(name = "common") {
        attributes.attribute(testAttribute, "common")
    }
    jvm(name = "desktop") {
        attributes.attribute(testAttribute, "desktop")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.runtime)
                implementation(libs.kotlinx.coroutines.core.library)
                implementation(libs.jackson.module.kotlin)
                implementation(libs.kotter.library)
            }
        }

        commonTest {
            koverReport {
                defaults {
                    mergeWith("release")
                }
            }
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
                api(libs.kover)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core.library)
                implementation(compose.desktop.currentOs)
                implementation(libs.kotter.library)
            }
        }

        val desktopTest by getting {
            koverReport {
                defaults {
                    mergeWith("release")
                }
            }
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
                api(libs.kover)
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
    }
}

compose.desktop {
    application {
        // Run configuration as:
        // Task = 'packageReleaseUberJarForCurrentOS'
        // Environment Variables = 'MAINCLASS=Main_terminalKt;PACKAGENAME=FittoniaTerminal'
        // Customize per build target e.g. "terminal, client engine, etc."
        mainClass = System.getenv("MAINCLASS")
        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.AppImage,
            )
            packageName = System.getenv("PACKAGENAME")
            packageVersion = "1.0"
        }
        from(kotlin.targets.getByName("desktop"))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34
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
