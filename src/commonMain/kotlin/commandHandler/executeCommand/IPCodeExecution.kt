package commandHandler.executeCommand

import OutputIO.printlnIO
import commandHandler.IPCodeCommand
import couldNotEncodeIp
import encodeIpAddress
import ipAddressCode
import printLine
import yourIpAddress
import java.net.Inet4Address

fun encodeIpCodeExecution(command: IPCodeCommand) {
    try {
        val address = Inet4Address.getLocalHost().hostAddress
        if (command.ioFormat) {
            printlnIO(output = encodeIpAddress(address))
        } else {
            printLine(text = yourIpAddress.format(address))
            printLine(text = ipAddressCode.format(encodeIpAddress(address)))
        }
    } catch (e: Exception) {
        printLine(text = couldNotEncodeIp)
    }
}
