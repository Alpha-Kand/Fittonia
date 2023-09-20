package commandHandler.executeCommand

import commandHandler.SendMessageCommand
import commandHandler.ServerFlags
import commandHandler.canContinue
import commandHandler.setupSendCommandClient

fun sendMessageExecution(command: SendMessageCommand) {
    val client = setupSendCommandClient(command = command)
    client.sendInt(ServerFlags.SEND_MESSAGE)
    if (canContinue(command = command, client = client)) {
        client.sendString(command.getMessage())
    }
}
