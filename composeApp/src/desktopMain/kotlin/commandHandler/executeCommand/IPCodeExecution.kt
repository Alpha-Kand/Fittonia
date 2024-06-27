package commandHandler.executeCommand

import OutputIO.printlnIO
import commandHandler.IPCodeCommand
import encodeIpAddress
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.could_not_encode_ip
import fittonia.composeapp.generated.resources.ip_address_code
import fittonia.composeapp.generated.resources.your_ip_address
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address

suspend fun encodeIpCodeExecution(command: IPCodeCommand) {
    try {
        val address = withContext(Dispatchers.IO) {
            Inet4Address.getLocalHost()
        }.hostAddress
        if (command.ioFormat) {
            printlnIO(text = encodeIpAddress(address))
        } else {
            printlnIO(Res.string.your_ip_address, address)
            printlnIO(Res.string.ip_address_code, encodeIpAddress(address))
        }
    } catch (e: Exception) {
        printlnIO(Res.string.could_not_encode_ip)
    }
}
