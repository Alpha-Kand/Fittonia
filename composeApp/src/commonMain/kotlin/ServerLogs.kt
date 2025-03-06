import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.hmeadow.fittonia.utility.debug
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.CoroutineContext

interface ServerLogs : CoroutineScope {
    val mLogs: MutableList<Log>
    val logsMutex: Mutex
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    fun log(log: String, jobId: Int? = null) = launchAndMutex {
        mLogs.add(Log(log, LogType.NORMAL, jobId))
    }

    fun logWarning(log: String, jobId: Int? = null) = launchAndMutex {
        mLogs.add(Log(log, LogType.WARNING, jobId))
    }

    fun logError(log: String, jobId: Int? = null) = launchAndMutex {
        mLogs.add(Log(log, LogType.ERROR, jobId))
    }

    fun logDebug(log: String, jobId: Int? = null) = launchAndMutex {
        mLogs.add(Log(log, LogType.DEBUG, jobId))
    }

    private fun launchAndMutex(block: () -> Unit) {
        launch(Dispatchers.Main) {
            logsMutex.withLock {
                block()
            }
        }
    }
}

enum class LogType {
    NORMAL,
    WARNING,
    ERROR,
    DEBUG,
}

class Log(
    private val time: ZonedDateTime,
    val message: String,
    val type: LogType,
    val jobId: Int?,
) {
    val timeStamp: String = "%1\$s %2\$sh %3\$sm %4\$ss".format(
        time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
        time.format(DateTimeFormatter.ofPattern("HH")),
        time.format(DateTimeFormatter.ofPattern("mm")),
        time.format(DateTimeFormatter.ofPattern("ss")),
    )

    init {
        // TODO Need a better way to see logs in app, don't want to expose them to system.out. - After release
        val typeString = when (type) {
            LogType.NORMAL -> ""
            LogType.WARNING -> "WARNING "
            LogType.ERROR -> "ERROR "
            LogType.DEBUG -> "DEBUG "
        }

        debug {
            jobId?.let {
                println("$typeString$timeStamp ($jobId): $message")
            } ?: println("$typeString$timeStamp: $message")
        }
    }

    constructor(message: String, type: LogType = LogType.NORMAL, jobId: Int? = null) : this(
        time = ZonedDateTime.now(),
        message = message,
        type = type,
        jobId = jobId,
    )
}
