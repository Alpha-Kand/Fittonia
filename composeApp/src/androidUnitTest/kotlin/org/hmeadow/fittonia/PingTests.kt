package org.hmeadow.fittonia

import UnitTest
import org.hmeadow.fittonia.models.Ping
import org.hmeadow.fittonia.models.PingStatus
import org.hmeadow.fittonia.models.mostRecent
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals

internal class PingTests : AndroidBaseMockkTest() {
    @Nested
    inner class MostRecent {

        @UnitTest
        fun mostRecentDifferentTimestamps() {
            val a = Ping(pingStatus = PingStatus.Success, requestTimestamp = 50)
            val b = Ping(pingStatus = PingStatus.Success, requestTimestamp = 100)
            assertEquals(expected = b, actual = mostRecent(a, b))
        }

        @UnitTest
        fun mostRecentSameTimestampsSamePriority() {
            val a = Ping(pingStatus = PingStatus.Success, requestTimestamp = 100)
            val b = Ping(pingStatus = PingStatus.ConnectionRefused, requestTimestamp = 100)
            assertEquals(expected = a, actual = mostRecent(a, b))
        }

        @UnitTest
        fun mostRecentSameTimestampsDifferentPriority() {
            val a = Ping(pingStatus = PingStatus.Processing, requestTimestamp = 100)
            val b = Ping(pingStatus = PingStatus.Success, requestTimestamp = 100)
            assertEquals(expected = b, actual = mostRecent(a, b))
        }
    }
}
