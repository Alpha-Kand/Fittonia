package commandHandler.executeCommand.sendExecution

import commandHandler.SendMessageCommand
import commandHandler.canContinueSendCommand
import commandHandler.setupSendCommandClient
import hmeadowSocket.HMeadowSocketClient

fun sendMessageExecutionClientEngine(command: SendMessageCommand, parent: HMeadowSocketClient) {
    val client = setupSendCommandClient(command = command)
    if (canContinueSendCommand(command = command, client = client, parent = parent)) {
        client.sendString(command.getMessage())
    }
}
