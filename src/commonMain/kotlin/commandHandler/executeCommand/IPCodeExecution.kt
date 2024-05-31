package commandHandler.executeCommand

import OutputIO.printlnIO
import commandHandler.IPCodeCommand
import encodeIpAddress
import printLine
import java.net.Inet4Address

fun encodeIpCodeExecution(command: IPCodeCommand) {
    try {
        val address = Inet4Address.getLocalHost().hostAddress
        if (command.ioFormat) {
            printlnIO(output = encodeIpAddress(address))
        } else {
            printLine(text = "Your IP address = $address")
            printLine(text = "IP address code = ${encodeIpAddress(address)}")
        }
    } catch (e: Exception) {
        printLine(text = "Could not encode IP address.")
    }
}
