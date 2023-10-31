package commandHandler.executeCommand

import com.varabyte.kotter.runtime.Session
import commandHandler.ServerCommand
import commandHandler.ServerFlags
import commandHandler.receivePassword
import commandHandler.sendConfirmation
import commandHandler.sendDeny
import hmeadowSocket.HMeadowSocketServer
import printLine

fun Session.serverExecution(command: ServerCommand) {
    printLine("Server started.")
    while (true) {
        printLine("⏳ Waiting for a client.")
        val server = HMeadowSocketServer.getServer(port = command.getPort())
        when (server.receiveInt()) {
            ServerFlags.SEND_FILES -> serverSendFilesExecution(server = server)

            ServerFlags.SEND_MESSAGE -> {
                server.sendConfirmation()
                if (!server.receivePassword()) return
                printLine("Received message from client.")
                printLine(server.receiveString(), color = 0xccc949) // Lightish yellow.
            }

            ServerFlags.ADD_DESTINATION -> {
                server.sendConfirmation()
                if (!server.receivePassword()) {
                    printLine("Client attempted to add this server as destination, password refused.")
                    return
                }
                if (server.receiveBoolean()) {
                    printLine("Client added this server as a destination.")
                } else {
                    printLine("Client failed to add this server as a destination.")
                }
            }

            else -> {
                server.sendDeny()
                printLine("Received invalid server command from client.")
            }
        }
    }
}
