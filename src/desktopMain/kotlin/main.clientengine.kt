import com.varabyte.kotter.foundation.text.Color
import commandHandler.AddCommand
import commandHandler.CommandHandler
import commandHandler.SendFilesCommand
import commandHandler.SendMessageCommand
import commandHandler.ServerFlagsString
import commandHandler.clientEnginePortArguments
import commandHandler.executeCommand.sendExecution.addExecutionClientEngine
import commandHandler.executeCommand.sendExecution.sendFilesExecutionClientEngine
import commandHandler.executeCommand.sendExecution.sendMessageExecutionClientEngine
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient
import java.net.InetAddress

fun main(args: Array<String>) {
    val port = args.find {
        it.startsWith("${clientEnginePortArguments.first()}=")
    }?.substringAfter('=')?.toIntOrNull() ?: throw IllegalArgumentException()

    val parent = HMeadowSocketClient(
        ipAddress = InetAddress.getByName("localhost"),
        port = port,
        timeoutMillis = 3000L,
    )
    try {
        when (val command = CommandHandler(args = args.toList()).getCommand()) {
            is SendFilesCommand -> sendFilesExecutionClientEngine(command = command, parent = parent)
            is SendMessageCommand -> sendMessageExecutionClientEngine(command = command, parent = parent)
            is AddCommand -> addExecutionClientEngine(command = command, parent = parent)
            else -> Unit
        }
    } catch (e: FittoniaError) {
        sendFittoniaError(e, parent = parent)
    } catch (e: HMeadowSocket.HMeadowSocketError) {
        sendHMSocketError(e, parent = parent)
    }
    parent.sendString(ServerFlagsString.DONE)
}

fun sendFittoniaError(e: FittoniaError, parent: HMeadowSocketClient) {
    parent.reportTextLine(e.getErrorMessage())
}

fun sendHMSocketError(e: HMeadowSocket.HMeadowSocketError, parent: HMeadowSocketClient) {
    parent.reportTextLine("Error: ", Color.RED)
    e.hmMessage?.let {
        parent.reportTextLine(text = it)
    }
    e.message?.let {
        parent.reportTextLine(text = "       $it")
    }
}
