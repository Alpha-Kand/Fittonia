import commandHandler.CommandHandler
import commandHandler.SendFilesCommand
import commandHandler.SendMessageCommand
import commandHandler.executeCommand.sendFilesExecution
import commandHandler.executeCommand.sendMessageExecution
import hmeadowSocket.HMeadowSocket

fun main(args: Array<String>) {
    try {
        when (val command = CommandHandler(args = args.toList()).getCommand()) {
            is SendFilesCommand -> sendFilesExecution(command = command)
            is SendMessageCommand -> sendMessageExecution(command = command)
            else -> Unit
        }
    } catch (e: FittoniaError) {
        reportFittoniaError(e)
    } catch (e: HMeadowSocket.HMeadowSocketError) {
        reportHMSocketError(e)
    }
}
