enum class Platform {
    ANDROID,
    DESKTOP,
}

expect fun getPlatform(): Platform
expect fun isDebug(): Boolean
