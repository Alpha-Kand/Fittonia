package commandHandler.executeCommand.sendExecution

import commandHandler.AddCommand
import commandHandler.ServerFlagsString
import commandHandler.canContinueSendCommand
import commandHandler.setupSendCommandClient
import hmeadowSocket.HMeadowSocketClient
import reportTextLine

fun addExecutionClientEngine(command: AddCommand, parent: HMeadowSocketClient) {
    val client = setupSendCommandClient(command = command)
    if (canContinueSendCommand(command = command, client = client, parent = parent)) {
        parent.sendString(ServerFlagsString.ADD_DESTINATION)
        client.sendBoolean(parent.receiveBoolean())
    } else {
        parent.reportTextLine(text = "Connected, but request refused.")
    }
}
