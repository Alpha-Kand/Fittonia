import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

interface ServerLogs {
    val mLogs: MutableList<Log>

    fun log(log: String, jobId: Int? = null) = synchronized(mLogs) {
        mLogs.add(Log(log, LogType.NORMAL, jobId))
    }

    fun logWarning(log: String, jobId: Int? = null) = synchronized(mLogs) {
        mLogs.add(Log(log, LogType.WARNING, jobId))
    }

    fun logError(log: String, jobId: Int? = null) = synchronized(mLogs) {
        mLogs.add(Log(log, LogType.ERROR, jobId))
    }

    fun logDebug(log: String, jobId: Int? = null) = synchronized(mLogs) {
        mLogs.add(Log(log, LogType.DEBUG, jobId))
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
        println("$timeStamp ($jobId): $message")
    }

    constructor(message: String, type: LogType = LogType.NORMAL, jobId: Int? = null) : this(
        time = ZonedDateTime.now(),
        message = message,
        type = type,
        jobId = jobId,
    )
}
