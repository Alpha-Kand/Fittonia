package org.hmeadow.fittonia

import Log
import LogType
import androidx.compose.runtime.mutableStateListOf

object AppLogs {
    val logs = mutableStateListOf<Log>()

    fun log(log: String, jobId: Int? = null) {
        logs.add(Log(log, LogType.NORMAL, jobId))
    }

    fun logWarning(log: String, jobId: Int? = null) = logs.add(Log(log, LogType.WARNING, jobId))
    fun logError(log: String, jobId: Int? = null) = logs.add(Log(log, LogType.ERROR, jobId))
    fun logDebug(log: String, jobId: Int? = null) = logs.add(Log(log, LogType.DEBUG, jobId))
}
