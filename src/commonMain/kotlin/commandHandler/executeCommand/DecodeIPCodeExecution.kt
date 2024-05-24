package commandHandler.executeCommand

import commandHandler.DecodeIPCodeCommand
import decodeIpAddress
import printLine

fun decodeIpCodeExecution(command: DecodeIPCodeCommand) {
    try {
        printLine(text = decodeIpAddress(ipAddress = command.getCode()))
    } catch (e: Exception) {
        printLine(e.message ?: "Could not decode the IP code.")
    }
}
