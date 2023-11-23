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
    printLine(text = "Server started.")
    while (true) {
        printLine(text = "⏳ Waiting for a client.")
        val server = HMeadowSocketServer.createServer(port = command.getPort())
        when (server.receiveInt()) {
            ServerFlags.SEND_FILES -> serverSendFilesExecution(server = server)

            ServerFlags.SEND_MESSAGE -> {
                server.sendConfirmation()
                if (!server.receivePassword()) return
                printLine(text = "Received message from client.")
                printLine(server.receiveString(), color = 0xccc949) // Lightish yellow.
            }

            ServerFlags.ADD_DESTINATION -> {
                server.sendConfirmation()
                if (!server.receivePassword()) {
                    printLine(text = "Client attempted to add this server as destination, password refused.")
                    return
                }
                if (server.receiveBoolean()) {
                    printLine(text = "Client added this server as a destination.")
                } else {
                    printLine(text = "Client failed to add this server as a destination.")
                }
            }

            else -> {
                server.sendDeny()
                printLine(text = "Received invalid server command from client.")
            }
        }
        server.close()
    }
}
