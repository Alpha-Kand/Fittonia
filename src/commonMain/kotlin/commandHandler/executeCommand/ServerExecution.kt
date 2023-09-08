package commandHandler.executeCommand

import commandHandler.ServerCommand
import commandHandler.ServerFlags
import commandHandler.receivePassword
import commandHandler.sendConfirmation
import commandHandler.sendDeny
import hmeadowSocket.HMeadowSocketServer

fun serverExecution(command: ServerCommand) {
    println("Server started, waiting for a client.")
    val server = HMeadowSocketServer(port = command.getPort())
    when (server.receiveInt()) {
        ServerFlags.SEND_FILES -> {
            server.sendConfirmation()
            if (!server.receivePassword()) return
            println("TODO From client: " + server.receiveString())
        }

        ServerFlags.SEND_STRING -> {
            server.sendConfirmation()
            if (!server.receivePassword()) return
            println("TODO SEND STRING")
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
