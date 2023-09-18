package commandHandler.executeCommand

import commandHandler.SendStringCommand
import commandHandler.ServerFlags
import commandHandler.canContinue
import commandHandler.setupSendCommandClient

fun sendStringExecution(command: SendStringCommand) {
    val client = setupSendCommandClient(command = command)
    client.sendInt(ServerFlags.SEND_STRING)
    if (canContinue(command = command, client = client)) {
        client.sendString(command.getString())
    }
}
