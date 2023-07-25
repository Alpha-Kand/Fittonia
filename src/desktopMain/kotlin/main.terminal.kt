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

                print("S Receiving: ")
                println(server.receiveInt().toString())
                println("S Sending: 5")
                server.sendLong(5)

                val stringMessage =
                    "Año Nuevo en Chile - A reading passage about celebrating New Year in Valparaíso, Chile."//😊
                println("S Sending: $stringMessage")
                server.sendString(message = stringMessage)
            }

            "client" -> {
                println("CLIENT")
                val client = HMeadowSocketClient(
                    ipAddress = InetAddress.getByName("localhost"),
                    port = PORT
                )

                println("C Sending: 4")
                client.sendInt(4)
                print("C Receiving: ")
                println(client.receiveLong().toString())
                print("C Receiving: ")
                println(client.receiveString())
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
