package org.hmeadow.fittonia.models

import java.time.Instant

data class Ping(
    val pingStatus: PingStatus,
    val requestTimestamp: Long = Instant.now().toEpochMilli(),
)

sealed interface PingStatus {
    val visibilityPriority: Int

    data object NoPing : PingStatus {
        override val visibilityPriority: Int = 0
    }

    data object Processing : PingStatus {
        override val visibilityPriority: Int = 0
    }

    data object Success : PingStatus {
        override val visibilityPriority: Int = 1
    }

    interface Failure : PingStatus {
        override val visibilityPriority: Int
            get() = 1
    }

    data object IncorrectPassword : Failure
    data object ConnectionRefused : Failure
    data object CouldNotConnect : Failure
    data object InternalBug : Failure
}

/**
 * Given a list of pings, returns the most up-to-date Ping.
 */
fun mostRecent(vararg pings: Ping): Ping = pings
    .groupBy { it.requestTimestamp }
    .maxBy { it.key } // Get most recent Pings with the same timestamp.
    .value
    .maxBy { it.pingStatus.visibilityPriority } // In case of a tie, return the status with the higher priority.
