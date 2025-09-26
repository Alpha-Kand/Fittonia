package org.hmeadow.fittonia

import Log
import LogType
import androidx.compose.runtime.mutableStateListOf
import isDebug
import org.hmeadow.fittonia.utility.debug

object AppLogs {
    val logs = mutableStateListOf<Log>()

    fun log(log: String, jobId: Int? = null) {
        logs.add(Log(log, LogType.NORMAL, jobId))
    }

    fun logWarning(log: String, jobId: Int? = null) {
        logs.add(Log(log, LogType.WARNING, jobId))
    }

    fun logError(log: String, jobId: Int? = null) {
        logs.add(Log(log, LogType.ERROR, jobId))
    }

    fun logDebug(log: String, jobId: Int? = null) {
        debug { logs.add(Log(log, LogType.DEBUG, jobId)) }
    }

    fun logBlock(log: String, type: LogType = LogType.NORMAL, jobId: Int? = null, block: () -> Boolean): Boolean {
        val newLog = Log(message = log, type = type, jobId = jobId)
        logs.add(newLog)
        return block().also { result ->
            newLog.success.value = result
        }
    }

    fun <T> logBlockTyped(
        log: String,
        type: LogType,
        jobId: Int? = null,
        block: () -> Pair<Boolean, T>,
    ): T {
        if (!isDebug() && type == LogType.DEBUG) return block().second
        val newLog = Log(message = log, type = type, jobId = jobId)
        logs.add(newLog)
        val result: Pair<Boolean, T> = block()
        newLog.success.value = result.first
        return result.second
    }

    fun <T> logBlockResult(
        log: String,
        type: LogType,
        jobId: Int? = null,
        block: () -> T,
    ): T {
        if (!isDebug() && type == LogType.DEBUG) return block()
        val newLog = Log(message = log, type = type, jobId = jobId)
        logs.add(newLog)
        return block().also {
            newLog.success.value = true
        }
    }

    suspend fun <T> logBlockResultS(
        log: String,
        type: LogType,
        jobId: Int? = null,
        block: suspend () -> T,
    ): T {
        val newLog = Log(message = log, type = type, jobId = jobId)
        logs.add(newLog)
        return block().also {
            newLog.success.value = true
        }
    }
}
