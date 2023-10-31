
import commandHandler.CommandHandler
import commandHandler.SendFilesCommand
import commandHandler.SendMessageCommand
import commandHandler.ServerFlags
import commandHandler.executeCommand.sendFilesExecution
import commandHandler.executeCommand.sendMessageExecutionClientEngine
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient
import java.lang.Thread.sleep
import java.net.InetAddress
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    var failAttempts = 50 // todo
    var parent: HMeadowSocketClient? = null
    while (parent == null) {
        try {
            parent = HMeadowSocketClient(
                ipAddress = InetAddress.getByName("localhost"),
                port = 10778,
            )
        } catch (e: HMeadowSocket.HMeadowSocketError) {
            sleep(100)
            println("DEBUG: Client failed connection $failAttempts")
            failAttempts -= 1
        }
        if (failAttempts <= 0) {
            println("DEBUG: Client dead")
            exitProcess(status = 1)
        }
    }

    try {
        when (val command = CommandHandler(args = args.toList()).getCommand()) {
            is SendFilesCommand -> sendFilesExecution(command = command, parent = parent)
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
        },
    )
    parent.reportTextLine(
        text = e.message?.let {
            "       $it"
        } ?: ".",
    )
}
