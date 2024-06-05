package commandHandler.executeCommand

import FittoniaError
import OutputIO.printlnIO
import blankIpCode
import commandHandler.DecodeIPCodeCommand
import couldNotDecodeIp
import decodeIpAddress

fun decodeIpCodeExecution(command: DecodeIPCodeCommand) {
    try {
        printlnIO(output = decodeIpAddress(ipAddress = command.getCode()))
    } catch (e: FittoniaError) {
        printlnIO(output = blankIpCode)
    } catch (e: Exception) {
        printlnIO(output = couldNotDecodeIp)
    }
}
