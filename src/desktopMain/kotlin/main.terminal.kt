import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketServer
import java.net.InetAddress

const val PORT = 2334

fun main(args: Array<String>) {
    println("Fittonia Terminal Program 2")

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
