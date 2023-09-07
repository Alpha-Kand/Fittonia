package commandHandler.executeCommand

import commandHandler.ServerCommand
import commandHandler.ServerFlags
import hmeadowSocket.HMeadowSocketServer

fun serverExecution(command: ServerCommand) {
    println("Server started, waiting for a client.")
    val server = HMeadowSocketServer(port = command.getPort())
    when (server.receiveInt()) {
        ServerFlags.SEND_FILES -> {
            server.sendInt(ServerFlags.CONFIRM)
            println("TODO From client: " + server.receiveString())
        }

        ServerFlags.SEND_STRING -> {
            println("TODO SEND STRING")
        }

        ServerFlags.ADD_DESTINATION -> {
            server.sendInt(ServerFlags.CONFIRM) // TODO: Add password checking.
        }

        else -> {
            server.sendInt(ServerFlags.DENY)
            println("Received invalid server command from client.")
        }
    }
}