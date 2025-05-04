package commandHandler

import OutputIO.printlnIO
import ServerCommandFlag
import SettingsManagerDesktop
import communicateCommandBoolean
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketClient

fun setupSendCommandClient(command: SendCommand): HMeadowSocketClient {
    val destination = SettingsManagerDesktop.settingsManager.findDestination(command.getDestination())
    return destination?.let {
        HMeadowSocketClient(
            ipAddress = destination.ip,
            port = command.getPort(),
            operationTimeoutMillis = 2000,
            handshakeTimeoutMillis = 2000,
        )
    } ?: HMeadowSocketClient(
        ipAddress = command.getIP(),
        port = command.getPort(),
        operationTimeoutMillis = 2000,
        handshakeTimeoutMillis = 2000,
    )
}

// TODO Sending files should be handled in DesktopServer. - After release
fun SendCommand.canContinueSendCommand(client: HMeadowSocketClient): Boolean {
    val destination = SettingsManagerDesktop.settingsManager.findDestination(this.getDestination())
    val accessCode = destination?.accessCode ?: this.getAccessCode()
    val commandFlag = when (this) {
        is SendFilesCommand -> ServerCommandFlag.SEND_FILES
        is SendMessageCommand -> ServerCommandFlag.SEND_MESSAGE
        is AddCommand -> ServerCommandFlag.ADD_DESTINATION
    }
    return client.communicateCommandBoolean(
        commandFlag = commandFlag,
        accessCode = accessCode,
        onSuccess = { },
        onAccessCodeRefused = { printlnIO("Server refused access code.") },
        onFailure = { printlnIO("Connected, but request refused.") },
    )
}
