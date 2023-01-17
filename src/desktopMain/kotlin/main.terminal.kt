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

                print("Receiving: ")
                println(server.receiveInt().toString())
                println("Sending: 5")
                server.sendLong(5)
            }

            "client" -> {
                println("CLIENT")
                val client = HMeadowSocketClient(
                    ipAddress = InetAddress.getByName("localhost"),
                    port = PORT
                )

                println("Sending: 4")
                client.sendInt(4)
                print("Receiving: ")
                println(client.receiveLong().toString())
            }
        }
    }catch(e: HMeadowSocket.HMeadowSocketError) {
        when(e.errorType) {
            HMeadowSocket.SocketErrorType.CLIENT_SETUP -> print("There was an error setting up CLIENT")
            HMeadowSocket.SocketErrorType.SERVER_SETUP -> print("There was an error setting up SERVER")
        }
        e.message?.let {
            println(" " + e.message)
        } ?: println(".")
    }
}
