package commandHandler.executeCommand.sendExecution

import commandHandler.SendMessageCommand
import commandHandler.ServerFlags
import commandHandler.canContinue
import commandHandler.setupSendCommandClient
import hmeadowSocket.HMeadowSocketClient

fun sendMessageExecutionClientEngine(command: SendMessageCommand, parent: HMeadowSocketClient) {
    val client = setupSendCommandClient(command = command)
    client.sendInt(ServerFlags.SEND_MESSAGE)
    if (canContinue(command = command, client = client, parent = parent)) {
        client.sendString(command.getMessage())
    }
}
