package commandHandler

import OutputIO.printlnIO
import ServerCommandFlag
import SettingsManagerDesktop
import hmeadowSocket.HMeadowSocketClient
import receiveApproval

fun setupSendCommandClient(command: SendCommand): HMeadowSocketClient {
    val destination = SettingsManagerDesktop.settingsManager.findDestination(command.getDestination())
    return destination?.let {
        HMeadowSocketClient(
            ipAddress = destination.ip,
            port = command.getPort(),
            operationTimeoutMillis = 2000,
            handshakeTimeoutMillis = 2000L,
        )
    } ?: HMeadowSocketClient(
        ipAddress = command.getIP(),
        port = command.getPort(),
        operationTimeoutMillis = 2000,
        handshakeTimeoutMillis = 2000L,
    )
}

fun HMeadowSocketClient.communicateCommand(
    commandFlag: ServerCommandFlag,
    password: String,
    onSuccess: () -> Unit,
    onPasswordRefused: () -> Unit,
    onFailure: () -> Unit,
): Boolean {
    sendString(message = commandFlag.text)
    return receiveApproval(
        onConfirm = {
            sendString(password)
            receiveApproval(
                onConfirm = {
                    onSuccess()
                    true
                },
                onDeny = {
                    onPasswordRefused()
                    false
                },
            )
        },
        onDeny = {
            onFailure()
            false
        },
    )
}

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
