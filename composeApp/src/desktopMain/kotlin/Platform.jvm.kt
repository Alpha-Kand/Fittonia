actual fun getPlatform(): Platform = Platform.DESKTOP
actual fun isDebug(): Boolean = false // TODO - After release
actual fun recordThrowable(throwable: Throwable) {
    // TODO - Report desktop errors somewhere.
}
