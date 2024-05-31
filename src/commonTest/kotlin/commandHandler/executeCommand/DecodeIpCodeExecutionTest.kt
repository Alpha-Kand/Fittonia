package commandHandler.executeCommand

import BaseMockkTest
import OutputIO
import UnitTest
import commandHandler.DecodeIPCodeCommand
import commandHandler.ipCodeArguments
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
    fun error() = runTest {
        decodeIpCodeExecution(
            command = DecodeIPCodeCommand().also {
                it.ioFormat = true
                it.addArg(argumentName = ipCodeArguments.first(), value = "nothing")
            },
        )
        TestCase.assertEquals(
            listOf("Could not decode the IP code."),
            OutputIO.flush(),
        )
    }
}
