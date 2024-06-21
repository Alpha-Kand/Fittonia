package commandHandler.executeCommand

import FittoniaError
import OutputIO.printlnIO
import blankIpCode
import commandHandler.DecodeIPCodeCommand
import couldNotDecodeIp
import decodeIpAddress

fun decodeIpCodeExecution(command: DecodeIPCodeCommand) {
    try {
        printlnIO(text = decodeIpAddress(ipAddress = command.getCode()))
    } catch (e: FittoniaError) {
        printlnIO(text = blankIpCode)
    } catch (e: Exception) {
        printlnIO(text = couldNotDecodeIp)
    }
}
