package commandHandler.executeCommand

import OutputIO.printlnIO
import commandHandler.DecodeIPCodeCommand
import decodeIpAddress

fun decodeIpCodeExecution(command: DecodeIPCodeCommand) {
    try {
        printlnIO(output = decodeIpAddress(ipAddress = command.getCode()))
    } catch (e: Exception) {
        printlnIO("Could not decode the IP code.")
    }
}
