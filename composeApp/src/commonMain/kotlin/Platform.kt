enum class Platform {
    ANDROID,
    DESKTOP,
}

expect fun getPlatform(): Platform
expect fun isDebug(): Boolean

/**
 * Reports the throwable to some external tooling/dashboard.
 * Android reports to Crashlytics.
 * Desktop reports to TODO.
 */
expect fun recordThrowable(throwable: Throwable)
