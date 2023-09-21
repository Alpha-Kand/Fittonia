package commandHandler.executeCommand

import commandHandler.ServerCommand
import commandHandler.ServerFlags
import commandHandler.receivePassword
import commandHandler.sendConfirmation
import commandHandler.sendDeny
import hmeadowSocket.HMeadowSocketServer

fun serverExecution(command: ServerCommand) {
    println("Server started, waiting for a client.")
    val server = HMeadowSocketServer.getServer(port = command.getPort())
    when (server.receiveInt()) {
        ServerFlags.SEND_FILES -> serverSendFilesExecution(server = server)

        ServerFlags.SEND_MESSAGE -> {
            server.sendConfirmation()
            if (!server.receivePassword()) return
            println("Received message from client.")
            println(server.receiveString())
        }

        ServerFlags.ADD_DESTINATION -> {
            server.sendConfirmation()
            if (!server.receivePassword()) {
                println("Client attempted to add this server as destination, password refused.")
                return
            }
            println("Client added this server as a destination.")
        }

        else -> {
            server.sendDeny()
            println("Received invalid server command from client.")
        }
    }
}
