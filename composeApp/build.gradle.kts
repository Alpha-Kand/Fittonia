import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kover)
    alias(libs.plugins.serialization)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
}

val testAttribute: Attribute<String> = Attribute.of("key", String::class.java)

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm(name = "desktop") {
        attributes.attribute(testAttribute, "desktop")
    }

    sourceSets {
        val desktopMain by getting
        val desktopTest by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.kotlin.serialization)
            implementation(libs.jackson.module.kotlin)
            implementation(libs.androidx.fragment.ktx)
            implementation(libs.androidx.activity.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.collections.immutable)
        }
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.preview)
            implementation(compose.runtime)
            implementation(compose.material)
            implementation(compose.foundation)
            implementation(libs.kotter.library)
            implementation(libs.kotlin.serialization)
            implementation(libs.jackson.module.kotlin)
            implementation(compose.components.resources)
            implementation(libs.androidx.material3.common)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.collections.immutable)
            implementation(compose.components.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(libs.mockk.library)
            implementation(libs.junit.jupiter.api)
            implementation(libs.junit.jupiter.platformlauncher)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.jackson.module.kotlin)

            implementation(libs.mockk.library)
            implementation(libs.junit)
            implementation(libs.junit.jupiter.api)
            implementation(libs.junit.jupiter.platformlauncher)
        }
        desktopTest.dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
            implementation(libs.kover)
            implementation(libs.mockk.library)
            implementation(libs.junit.jupiter)
            implementation(libs.junit)
            implementation(libs.junit.jupiter.api)
            implementation(libs.junit.jupiter.params)
            implementation(libs.junit.jupiter.engine)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.junit.jupiter.platformlauncher)
        }
    }
    sourceSets.androidMain.dependencies {
        implementation(libs.mockk.library)
        implementation(libs.junit.jupiter.api)
        implementation(kotlin("test"))
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

android {
    namespace = "org.hmeadow.fittonia"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "org.hmeadow.fittonia"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles.add(file("proguard-rules.pro"))
        }
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            buildConfigField("boolean", "ENABLE_CRASHLYTICS", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.hmeadow.fittonia"
            packageVersion = "1.0.0"
        }
    }
}

val ktlint = configurations.create("ktlint")

dependencies {
    ktlint(libs.ktlintlib)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlin.serialization)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.material3.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)

    androidTestImplementation(libs.junit)
    androidTestImplementation(kotlin("test"))
    androidTestImplementation(libs.mockk.library)
    androidTestImplementation(libs.junit.jupiter.api)
    androidTestImplementation(libs.junit.jupiter.platformlauncher)
}

kover {
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
            }
        }
    }
}

val ktlintOutputDir = "${project.layout.buildDirectory}/reports/ktlint/"
val ktlintInputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))

val ktlintCheck by tasks.creating(JavaExec::class) {
    inputs.files(ktlintInputFiles)
    outputs.dir(ktlintOutputDir)

    description = "Check Kotlin code style."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")

    args = listOf(
        "src/**/*.kt",
        "--reporter=plain",
        "--reporter=html,output=${project.layout.buildDirectory}/ktlint.html",
        "--baseline=ktlint-baseline.xml",
    )
}
