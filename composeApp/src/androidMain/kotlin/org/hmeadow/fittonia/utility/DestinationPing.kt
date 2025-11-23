package org.hmeadow.fittonia.utility

import SettingsManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.hmeadow.fittonia.models.Ping
import org.hmeadow.fittonia.models.PingStatus
import org.hmeadow.fittonia.models.mostRecent
import java.time.Instant

private val pingMutex: Mutex = Mutex()

interface DestinationPing {
    val ping: MutableStateFlow<Ping>

    suspend fun updatePingAtomically(newPing: Ping) {
        pingMutex.withLock {
            ping.value = mostRecent(ping.value, newPing)
        }
    }

    fun CoroutineScope.updatePing(
        destination: SettingsManager.Destination,
        port: Int,
        onPing: suspend (ip: String, port: Int, accessCode: String, requestTimestamp: Long) -> Ping,
    ) {
        updatePing(ip = destination.ip, accessCode = destination.accessCode, port = port, onPing = onPing)
    }

    fun CoroutineScope.updatePing(
        ip: String,
        accessCode: String,
        port: Int,
        onPing: suspend (ip: String, port: Int, accessCode: String, requestTimestamp: Long) -> Ping,
    ) {
        if (ip.isNotBlank() && accessCode.isNotBlank()) {
            launch {
                val timestamp = Instant.now().toEpochMilli()
                updatePingAtomically(newPing = Ping(PingStatus.Processing, timestamp))
                updatePingAtomically(
                    newPing = onPing(
                        ip,
                        port, // TODO remove Port type. - After release
                        accessCode, // TODO before release - check if access code should be string and not bytearray.
                        Instant.now().toEpochMilli().let { now ->
                            if (now == timestamp) {
                                timestamp + 1
                            } else {
                                now
                            }
                        },
                    ),
                )
            }
        }
    }
}

val DestinationPing.pingStatus: PingStatus
    @Composable
    get() = this.ping.collectAsState(Ping(PingStatus.NoPing)).value.pingStatus
