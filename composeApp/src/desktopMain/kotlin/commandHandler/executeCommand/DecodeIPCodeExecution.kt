package commandHandler.executeCommand

import FittoniaError
import OutputIO.printlnIO
import commandHandler.DecodeIPCodeCommand
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.blank_ip_code
import fittonia.composeapp.generated.resources.could_not_decode_ip
import org.hmeadow.fittonia.utility.decodeIpAddress

suspend fun decodeIpCodeExecution(command: DecodeIPCodeCommand) {
    try {
        printlnIO(text = decodeIpAddress(ipAddress = command.getCode()))
    } catch (_: FittoniaError) {
        printlnIO(Res.string.blank_ip_code)
    } catch (_: Exception) {
        printlnIO(Res.string.could_not_decode_ip)
    }
}
