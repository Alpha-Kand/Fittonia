package org.hmeadow.fittonia.utility

import UnitTest
import org.hmeadow.fittonia.CommonBaseMockkTest
import org.junit.jupiter.api.Assertions

private class ByteAmountConversionsTest : CommonBaseMockkTest() {
    val oneKB: Long = 1000L
    val oneMB: Long = oneKB * 1000L
    val oneGB: Long = oneMB * 1000L
    val oneTB: Long = oneGB * 1000L
    val onePB: Long = oneTB * 1000L

    @UnitTest
    fun bytesToMegaBytesTest() {
        Assertions.assertEquals("1", bytesToMegaBytes(bytes = oneMB))
        Assertions.assertEquals("5", bytesToMegaBytes(bytes = oneMB * 5))
        Assertions.assertEquals("5.5", bytesToMegaBytes(bytes = (oneMB * 5.5f).toLong()))
        Assertions.assertEquals("1000", bytesToMegaBytes(bytes = oneGB))
        Assertions.assertEquals("1001", bytesToMegaBytes(bytes = oneGB + oneMB))
        Assertions.assertEquals(
            "1001.55",
            bytesToMegaBytes(bytes = oneGB + oneMB + (oneMB * 0.55).toLong()),
        )
        Assertions.assertEquals("1000000", bytesToMegaBytes(bytes = oneTB))
        Assertions.assertEquals("1000000000", bytesToMegaBytes(bytes = onePB))
    }
}
