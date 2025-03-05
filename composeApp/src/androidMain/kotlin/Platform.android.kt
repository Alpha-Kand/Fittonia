import org.hmeadow.fittonia.BuildConfig

actual fun getPlatform(): Platform = Platform.ANDROID
actual fun isDebug(): Boolean = BuildConfig.DEBUG
