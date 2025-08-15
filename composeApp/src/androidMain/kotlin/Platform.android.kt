import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.hmeadow.fittonia.BuildConfig

actual fun getPlatform(): Platform = Platform.ANDROID
actual fun isDebug(): Boolean = BuildConfig.DEBUG
actual fun recordThrowable(throwable: Throwable) {
    FirebaseCrashlytics.getInstance().recordException(throwable)
}
