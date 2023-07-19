pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }

    plugins {
        kotlin("multiplatform").version("1.7.10")
        kotlin("android").version("1.7.10")
        id("org.jetbrains.compose").version("1.4.1")
    }
}

rootProject.name = "fittonia"
