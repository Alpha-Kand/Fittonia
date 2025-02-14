import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kover)
    alias(libs.plugins.serialization)
}

val testAttribute: Attribute<String> = Attribute.of("key", String::class.java)

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop") {
        attributes.attribute(testAttribute, "desktop")
    }

    sourceSets {
        val desktopMain by getting
        val desktopTest by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlin.serialization)
            implementation(libs.androidx.activity.ktx)
            implementation(libs.androidx.fragment.ktx)
        }
        commonMain.dependencies {
            implementation(compose.preview)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotter.library)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlin.serialization)
            implementation(libs.androidx.material3.common)
        }
        commonTest.dependencies {
            api(libs.mockk.library)
            api(libs.junit.jupiter.api)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.jackson.module.kotlin)

            api(libs.mockk.library)
            api(libs.junit.jupiter.api)
            implementation(libs.junit)
        }
        desktopTest.dependencies {
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
    sourceSets.androidMain.dependencies {
        api(libs.mockk.library)
        api(libs.junit.jupiter.api)
        implementation(kotlin("test"))
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
}

tasks.withType<Test> {
    useJUnitPlatform()
}

android {
    namespace = "org.hmeadow.fittonia"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

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
            isMinifyEnabled = false
            isDebuggable = false
        }
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            buildConfigField("boolean", "ENABLE_CRASHLYTICS", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.documentfile)
    ktlint(libs.ktlintlib)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.serialization)

    androidTestImplementation(libs.junit)
    androidTestImplementation(kotlin("test"))
    androidTestImplementation(libs.mockk.library)
    androidTestImplementation(libs.junit.jupiter.api)
}

kover {
    reports{
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
