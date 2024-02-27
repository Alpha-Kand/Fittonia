import commandHandler.executeCommand.handleCommand
import commandHandler.executeCommand.serverSendFilesExecution
import commandHandler.serverEnginePortArguments
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketServer
import java.net.InetAddress

fun main(args: Array<String>) {
    val port = args.find {
        it.startsWith("${serverEnginePortArguments.first()}=")
    }?.substringAfter('=')?.toIntOrNull() ?: throw IllegalArgumentException()

    val serverParent = HMeadowSocketClient(
        ipAddress = InetAddress.getByName("localhost"),
        port = port,
        timeoutMillis = 3000L,
    )
    val serverEngine = HMeadowSocketServer.createServerAnyPort(startingPort = 10978) { foo ->
        serverParent.sendInt(foo)
    }

    serverEngine.handleCommand(
        onSendFilesCommand = {
            // TODO it
            serverEngine.serverSendFilesExecution(serverParent = serverParent)
        },
        onSendMessageCommand = {
            // TODO it
            printLine(text = "Received message from client.")
            printLine(serverEngine.receiveString(), color = 0xccc949) // Lightish yellow.
        },
        onAddDestination = {
            // TODO it
            if (!it) {
                println("Client attempted to add this server as destination, password refused.")//todo
            } else {
                if (serverEngine.receiveBoolean()) {
                    println("Client added this server as a destination.")
                } else {
                    println("Client failed to add this server as a destination.")//todo
                }
            }
        },
        onInvalidCommand = {
            println("Received invalid server command from client.")//todo
        }
    )
    serverEngine.close()
}