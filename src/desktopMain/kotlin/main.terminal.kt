import commandHandler.AddCommand
import commandHandler.CommandHandler
import commandHandler.DumpCommand
import commandHandler.ListDestinationsCommand
import commandHandler.RemoveCommand
import commandHandler.SendFilesCommand
import commandHandler.ServerCommand
import commandHandler.SetDefaultPortCommand
import commandHandler.executeCommand.addExecution
import commandHandler.executeCommand.listDestinationsExecution
import commandHandler.executeCommand.removeExecution
import commandHandler.executeCommand.sendFilesExecution
import commandHandler.executeCommand.serverExecution
import commandHandler.executeCommand.setDefaultPortExecution
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketServer
import settingsManager.SettingsManager
import java.net.InetAddress

fun main(args: Array<String>) {
    SettingsManager.settingsManager.saveSettings()
    when (val command = CommandHandler(args = args).getCommand()) {
        is AddCommand -> addExecution(command = command)
        is RemoveCommand -> removeExecution(command = command)
        is ListDestinationsCommand -> listDestinationsExecution(command = command)
        is DumpCommand -> dumpExecution(command = command)
        is ServerCommand -> serverExecution(command = command)
        is SendFilesCommand -> sendFilesExecution(command = command)
        is SetDefaultPortCommand -> setDefaultPortExecution(command = command)
        else -> throw IllegalStateException("No valid command detected.")
    }

    return

    try {
        when (args[0]) {
            "server" -> {
                println("SERVER")
                val server = HMeadowSocketServer(port = 2334)

                print("Server Receiving: ")
                println(server.receiveInt().toString())
                println("Server Sending: 5")
                server.sendLong(5)

                val stringMessage =
                    "Año Nuevo en Chile - A reading passage about celebrating New Year in Valparaíso, Chile." // 😊
                println("Server Sending: $stringMessage")
                server.sendString(message = stringMessage)

                println("Server Receiving: \"file.txt\"")
                server.receiveFile(destination = "/home/hunterneo/Desktop/TRANSFER/RECEIVE/")

                println("Server Sending: \"song.mp3\"")
                server.sendFile(filePath = "/home/hunterneo/Desktop/TRANSFER/SEND/song.mp3")

                println("Server shutdown normally")
            }

            "client" -> {
                println("CLIENT")
                val client = HMeadowSocketClient(
                    ipAddress = InetAddress.getByName("localhost"),
                    port = 2334,
                )

                println("Client Sending: 4")
                client.sendInt(4)
                print("Client Receiving: ")
                println(client.receiveLong().toString())
                print("Client Receiving: ")
                println(client.receiveString())

                println("Client Sending: \"file.txt\"")
                client.sendFile(filePath = "/home/hunterneo/Desktop/TRANSFER/SEND/file.txt")

                println("Client Receiving: \"song.mp3\"")
                client.receiveFile(destination = "/home/hunterneo/Desktop/TRANSFER/RECEIVE/")

                println("Client shutdown normally")
            }
        }
    } catch (e: HMeadowSocket.HMeadowSocketError) {
        when (e.errorType) {
            HMeadowSocket.SocketErrorType.CLIENT_SETUP -> print("There was an error setting up CLIENT")
            HMeadowSocket.SocketErrorType.SERVER_SETUP -> print("There was an error setting up SERVER")
        }
        e.message?.let {
            println(" " + e.message)
        } ?: println(".")
    }
}
