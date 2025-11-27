package org.hmeadow.fittonia.utility

import UnitTest
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import org.hmeadow.fittonia.CommonBaseMockkTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

private class EncodeDecodeIPTest : CommonBaseMockkTest() {
    private val successesIPToCode = listOf(
        "123.123.123.123" to "alpha.blind.123",
        "0.0.0.0" to "bedding.bedding.0",
        "255.255.255.255" to "mapping.mapping.255",
        "17.136.51.0" to "a.a.0",
        "17.136.51.170" to "a.a.170",
    )

    private val successesCodeToIP = listOf(
        "alpha.BLIND.123" to "123.123.123.123",
        "BEDDING.bedding.0" to "0.0.0.0",
        "mapping.MAPping.255" to "255.255.255.255",
        "a.a.0" to "17.136.51.0",
        "a.a.170" to "17.136.51.170",
        "A.A.0" to "17.136.51.0",
        "A.A.170" to "17.136.51.170",
    )

    private val encodingFails = listOf(
        "123.456.789",
        "123.456.789.",
        "123.456.789.123.",
        "123.123.1d3.123",
        ".1.123.123.123",
        "256.123.123.123",
        "",
        "123.123.123.123.123",
    )

    private val decodingFails = listOf(
        "a.0",
        "a.a",
        "a.a..1",
        "a.0.a",
        "a.a.a.0",
        "",
        "0.0.0",
    )

    @UnitTest
    fun encodeDecodeSuccess() {
        successesIPToCode.forEach {
            val code = encodeIpAddress(ipAddress = it.first)
            Assertions.assertEquals(it.second, code)
            Assertions.assertEquals(it.first, decodeIpAddress(ipAddress = code))
        }
        successesCodeToIP.forEach {
            val code = decodeIpAddress(ipAddress = it.first)
            Assertions.assertEquals(it.second, code)
            Assertions.assertEquals(it.first.toLowerCase(Locale.current), encodeIpAddress(ipAddress = code))
        }
    }

    @UnitTest
    fun encodingFails() {
        encodingFails.forEach {
            assertThrows<IllegalArgumentException> { encodeIpAddress(ipAddress = it) }
        }
    }

    @UnitTest
    fun decodingFails() {
        decodingFails.forEach {
            assertThrows<IllegalArgumentException> { decodeIpAddress(ipAddress = it) }
        }
    }

    @UnitTest
    fun verifyIPAddress() {
        (successesIPToCode + successesCodeToIP).forEach {
            assertDoesNotThrow {
                it.first.verifyIPAddress()
                it.second.verifyIPAddress()
            }
        }
        (encodingFails + decodingFails).forEach {
            assertThrows<IllegalArgumentException> { it.verifyIPAddress() }.message?.let { message ->
                // Ensure details of IP is redacted.
                Assertions.assertFalse(message.substringAfter(':').contains(Regex("[a-zA-Z0-9]")))
            }
        }
    }
}
