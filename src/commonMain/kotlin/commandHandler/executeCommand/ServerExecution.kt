package commandHandler.executeCommand

import com.varabyte.kotter.runtime.Session
import commandHandler.ServerCommand
import commandHandler.ServerCommandFlag
import commandHandler.ServerCommandFlag.Companion.toCommandFlag
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketServer
import printLine
import sendConfirmation
import sendDeny
import settingsManager.SettingsManager

private fun HMeadowSocketServer.passwordIsValid() = SettingsManager.settingsManager.checkPassword(receiveString())

fun HMeadowSocketServer.handleCommand(
    onSendFilesCommand: (Boolean) -> Unit,
    onSendMessageCommand: (Boolean) -> Unit,
    onAddDestination: (Boolean) -> Unit,
    onInvalidCommand: () -> Unit,
) {
    val command: ServerCommandFlag
    try {
        command = receiveInt().toCommandFlag()
    } catch (e: Exception) {
        sendDeny()
        onInvalidCommand()
        return
    }
    sendConfirmation()
    val passwordIsValid = passwordIsValid()
    sendApproval(choice = passwordIsValid)
    when (command) {
        ServerCommandFlag.SEND_FILES -> onSendFilesCommand(passwordIsValid)
        ServerCommandFlag.SEND_MESSAGE -> onSendMessageCommand(passwordIsValid)
        ServerCommandFlag.ADD_DESTINATION -> onAddDestination(passwordIsValid)
    }
}

fun HMeadowSocket.sendApproval(choice: Boolean) {
    if (choice) {
        sendConfirmation()
    } else {
        sendDeny()
    }
}

fun Session.serverExecution(command: ServerCommand) {
    printLine(text = "Server started.")
    while (true) {
        printLine(text = "⏳ Waiting for a client.")
        val server = HMeadowSocketServer.createServer(
            port = command.getPort(),
            timeoutMillis = 2000,
        )
        server.handleCommand(
            onSendFilesCommand = {
                //todo it
                serverSendFilesExecution(server = server)
            },
            onSendMessageCommand = {
                //todo it
                printLine(text = "Received message from client.")
                printLine(server.receiveString(), color = 0xccc949) // Lightish yellow.
            },
            onAddDestination = {
                //todo it
                if (!it) {
                    printLine(text = "Client attempted to add this server as destination, password refused.")
                } else {
                    if (server.receiveBoolean()) {
                        printLine(text = "Client added this server as a destination.")
                    } else {
                        printLine(text = "Client failed to add this server as a destination.")
                    }
                }
            },
            onInvalidCommand = {
                printLine(text = "Received invalid server command from client.")
            }
        )
        server.close()
    }
}
