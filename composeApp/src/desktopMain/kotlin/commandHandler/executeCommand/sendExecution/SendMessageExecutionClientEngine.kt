package commandHandler.executeCommand.sendExecution

import OutputIO.printlnIO
import commandHandler.SendMessageCommand
import commandHandler.canContinueSendCommand
import commandHandler.receiveConfirmation
import commandHandler.setupSendCommandClient

fun sendMessageExecution(command: SendMessageCommand) {
    val client = setupSendCommandClient(command = command)
    if (command.canContinueSendCommand(client = client)) {
        client.sendString(command.getMessage())
        if (client.receiveConfirmation()) {
            printlnIO("Message sent successfully.")
        } else {
            printlnIO("Something went wrong. Message may not have been sent.")
        }
    }
}
