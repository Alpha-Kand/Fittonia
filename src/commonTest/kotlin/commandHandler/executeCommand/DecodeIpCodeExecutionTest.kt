package commandHandler.executeCommand

import BaseMockkTest
import OutputIO
import UnitTest
import blankIpCode
import commandHandler.DecodeIPCodeCommand
import commandHandler.ipCodeArguments
import couldNotDecodeIp
import junit.framework.TestCase
import kotlinx.coroutines.test.runTest

private class DecodeIpCodeExecutionTest : BaseMockkTest() {

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
            listOf(couldNotDecodeIp),
            OutputIO.flush(),
        )
    }

    @UnitTest
    fun errorEmptyCode() = runTest {
        decodeIpCodeExecution(
            command = DecodeIPCodeCommand().also { it.ioFormat = true },
        )
        TestCase.assertEquals(
            listOf(blankIpCode),
            OutputIO.flush(),
        )
    }
}
