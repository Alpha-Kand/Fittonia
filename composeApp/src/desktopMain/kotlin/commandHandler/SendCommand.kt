package commandHandler

import OutputIO.printlnIO
import ServerCommandFlag
import SettingsManagerDesktop
import communicateCommand
import hmeadowSocket.HMeadowSocketClient

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
    val password = destination?.password ?: this.getPassword()
    val commandFlag = when (this) {
        is SendFilesCommand -> ServerCommandFlag.SEND_FILES
        is SendMessageCommand -> ServerCommandFlag.SEND_MESSAGE
        is AddCommand -> ServerCommandFlag.ADD_DESTINATION
    }
    return client.communicateCommand(
        commandFlag = commandFlag,
        password = password,
        onSuccess = { },
        onPasswordRefused = { printlnIO("Server refused password.") },
        onFailure = { printlnIO("Connected, but request refused.") },
    )
}
