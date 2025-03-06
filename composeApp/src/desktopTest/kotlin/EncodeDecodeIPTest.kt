import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows

private class EncodeDecodeIPTest : DesktopBaseMockkTest() {

    @UnitTest
    fun encodeDecodeSuccess() = runTest {
        "123.123.123.123".let {
            val code = encodeIpAddress(ipAddress = it)
            assertEquals("alpha-blind-7b", code)
            assertEquals(it, decodeIpAddress(ipAddress = code))
        }
        "0.0.0.0".let {
            val code = encodeIpAddress(ipAddress = it)
            assertEquals("bedding-bedding-00", code)
            assertEquals(it, decodeIpAddress(ipAddress = code))
        }
        "255.255.255.255".let {
            val code = encodeIpAddress(ipAddress = it)
            assertEquals("mapping-mapping-ff", code)
            assertEquals(it, decodeIpAddress(ipAddress = code))
        }

        assertEquals("17.136.51.0", decodeIpAddress(ipAddress = "a-a-0"))
        assertEquals("17.136.51.170", decodeIpAddress(ipAddress = "A-A-AA"))
    }

    @UnitTest
    fun encodingFails() = runTest {
        assertThrows<IllegalArgumentException> { encodeIpAddress(ipAddress = "123.456.789") }
        assertThrows<IllegalArgumentException> { encodeIpAddress(ipAddress = "123.456.789.") }
        assertThrows<IllegalArgumentException> { encodeIpAddress(ipAddress = "123.456.789.123.") }
        assertThrows<IllegalArgumentException> { encodeIpAddress(ipAddress = "123.123.1d3.123") }
        assertThrows<IllegalArgumentException> { encodeIpAddress(ipAddress = "-1.123.123.123") }
        assertThrows<IllegalArgumentException> { encodeIpAddress(ipAddress = "256.123.123.123") }
        assertThrows<IllegalArgumentException> { encodeIpAddress(ipAddress = "") }
        assertThrows<IllegalArgumentException> { encodeIpAddress(ipAddress = "123.123.123.123.123") }
    }

    @UnitTest
    fun decodingFails() = runTest {
        assertThrows<IllegalArgumentException> { decodeIpAddress(ipAddress = "a-0") }
        assertThrows<IllegalArgumentException> { decodeIpAddress(ipAddress = "a-a") }
        assertThrows<IllegalArgumentException> { decodeIpAddress(ipAddress = "a-a-100") }
        assertThrows<IllegalArgumentException> { decodeIpAddress(ipAddress = "a-a--1") }
        assertThrows<IllegalArgumentException> { decodeIpAddress(ipAddress = "a-0-a") }
        assertThrows<IllegalArgumentException> { decodeIpAddress(ipAddress = "a-a-a-0") }
        assertThrows<IllegalArgumentException> { decodeIpAddress(ipAddress = "") }
        assertThrows<IllegalArgumentException> { decodeIpAddress(ipAddress = "0-0-0") }
    }
}
