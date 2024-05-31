import commandHandler.executeCommand.handleCommandServerEngine
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

    serverEngine.handleCommandServerEngine(serverParent = serverParent)
    serverEngine.close()
}
