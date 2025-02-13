package commandHandler.executeCommand

import DesktopBaseMockkTest
import OutputIO
import UnitTest
import commandHandler.DecodeIPCodeCommand
import commandHandler.ipCodeArguments
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.blank_ip_code
import fittonia.composeapp.generated.resources.could_not_decode_ip
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString

private class DecodeIpCodeExecutionTest : DesktopBaseMockkTest() {

    @UnitTest
    fun default() = runTest {
        decodeIpCodeExecution(
            command = DecodeIPCodeCommand().also {
                it.ioFormat = true
                it.addArg(argumentName = ipCodeArguments.first(), value = "twin-theatre-60")
            },
        )
        TestCase.assertEquals(
            listOf("192.168.205.96"),
            OutputIO.flush(),
        )
    }

    @UnitTest
    fun errorInvalidCode() = runTest {
        decodeIpCodeExecution(
            command = DecodeIPCodeCommand().also {
                it.ioFormat = true
                it.addArg(argumentName = ipCodeArguments.first(), value = "nothing")
            },
        )
        TestCase.assertEquals(
            listOf(getString(Res.string.could_not_decode_ip)),
            OutputIO.flush(),
        )
    }

    @UnitTest
    fun errorEmptyCode() = runTest {
        decodeIpCodeExecution(
            command = DecodeIPCodeCommand().also { it.ioFormat = true },
        )
        TestCase.assertEquals(
            listOf(getString(Res.string.blank_ip_code)),
            OutputIO.flush(),
        )
    }
}
