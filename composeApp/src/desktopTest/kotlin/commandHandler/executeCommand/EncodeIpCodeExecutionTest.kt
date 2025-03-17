package commandHandler.executeCommand

import DesktopBaseMockkTest
import OutputIO
import UnitTest
import commandHandler.IPCodeCommand
import io.mockk.every
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import java.net.Inet4Address

private class EncodeIpCodeExecutionTest : DesktopBaseMockkTest() {

    @UnitTest
    fun default() = runTest {
        mockkStatic(Inet4Address::class)
        every { Inet4Address.getLocalHost().hostAddress } returns "192.168.205.96"

        encodeIpCodeExecution(
            command = IPCodeCommand().also {
                it.machineReadableOutput.ioFormat = true
            },
        )
        Assertions.assertEquals(
            listOf("twin-theatre-60"),
            OutputIO.flush(),
        )
    }
}
