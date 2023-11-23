
import commandHandler.CommandHandler
import commandHandler.SendFilesCommand
import commandHandler.SendMessageCommand
import commandHandler.ServerFlags
import commandHandler.executeCommand.sendExecution.sendFilesExecutionClientEngine
import commandHandler.executeCommand.sendExecution.sendMessageExecutionClientEngine
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient
import java.net.InetAddress

fun main(args: Array<String>) {
    val port = args.find {
        it.startsWith("clientengineport=")
    }?.substringAfter('=')?.toIntOrNull() ?: throw IllegalArgumentException() // exitProcess(1)

    val parent = HMeadowSocketClient(
        ipAddress = InetAddress.getByName("localhost"),
        port = port,
        timeoutMillis = 3000L,
    )

    try {
        when (val command = CommandHandler(args = args.toList()).getCommand()) {
            is SendFilesCommand -> sendFilesExecutionClientEngine(command = command, parent = parent)
            is SendMessageCommand -> sendMessageExecutionClientEngine(command = command, parent = parent)
            else -> Unit
        }
    } catch (e: FittoniaError) {
        sendFittoniaError(e, parent = parent)
    } catch (e: HMeadowSocket.HMeadowSocketError) {
        sendHMSocketError(e, parent = parent)
    }
    parent.sendInt(ServerFlags.DONE)
}

fun sendFittoniaError(e: FittoniaError, parent: HMeadowSocketClient) {
    parent.reportTextLine(e.getErrorMessage())
}

fun sendHMSocketError(e: HMeadowSocket.HMeadowSocketError, parent: HMeadowSocketClient) {
    parent.reportTextLine(
        text = when (e.errorType) {
            HMeadowSocket.SocketErrorType.CLIENT_SETUP -> "There was an error setting up CLIENT"
            HMeadowSocket.SocketErrorType.SERVER_SETUP -> "There was an error setting up SERVER"
            HMeadowSocket.SocketErrorType.COULD_NOT_BIND_SERVER_TO_GIVEN_PORT ->
                "Could not create server on given port."

            HMeadowSocket.SocketErrorType.COULD_NOT_FIND_AVAILABLE_PORT -> "Could not find any available ports."
        },
    )
    parent.reportTextLine(
        text = e.message?.let {
            "       $it"
        } ?: ".",
    )
}
