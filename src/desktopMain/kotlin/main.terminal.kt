import commandHandler.AddCommand
import commandHandler.CommandHandler
import commandHandler.DumpCommand
import commandHandler.ListDestinationsCommand
import commandHandler.RemoveCommand
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketServer
import settingsManager.SettingsManager
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Paths

const val PORT = 2334

fun main(args: Array<String>) {
    println("!> Fittonia Terminal Program 2 <!")
    println()

    val settingsManager = SettingsManager.settingsManager
    SettingsManager.settingsManager.saveSettings()
    when (val command = CommandHandler(args = args).getCommand()) {
        is AddCommand -> {
            settingsManager.addDestination(
                name = command.getName(),
                ip = command.getIP(),
                password = command.getPassword(),
            )
        }

        is RemoveCommand -> {
            settingsManager.removeDestination(name = command.getName())
        }

        is ListDestinationsCommand -> {
            println()
            val printDestination: (SettingsManager.SettingsData.Destination) -> Unit = {
                println("Name: " + it.name)
                println("IP: " + it.ip)
                println()
            }
            command.getName()?.let { searchName ->
                settingsManager.settings.destinations.find { it.name == searchName }?.let {
                    printDestination(it)
                } ?: run {
                    println("No destination found with that name.")
                    println()
                }
            } ?: settingsManager.settings.destinations.forEach {
                printDestination(it)
            }
        }

        is DumpCommand -> {
            command.getDumpPath()?.let {
                val path = Paths.get(it).toAbsolutePath()
                if (Files.exists(path)) {
                    if (Files.isDirectory(path)) {
                        settingsManager.setDumpPath(path.toString())

                        if (!Files.list(path).findFirst().isPresent) {
                            println("Warning: New dump path is not empty.")
                        }
                    } else {
                        println("Supplied path was not a valid directory.")
                    }
                } else {
                    println("Supplied path does not exist.")
                }
            } ?: run {
                if (settingsManager.settings.dumpPath.isEmpty()) {
                    println("No dump path set.")
                } else {
                    println("Current dump path: " + settingsManager.settings.dumpPath)
                }
            }
        }

        else -> throw IllegalStateException("No valid command detected.")
    }

    return

    try {
        when (args[0]) {
            "server" -> {
                println("SERVER")
                val server = HMeadowSocketServer(port = PORT)

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
                    port = PORT,
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
