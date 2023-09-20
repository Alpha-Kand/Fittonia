import commandHandler.CommandHandler
import commandHandler.SendFilesCommand
import commandHandler.SendMessageCommand
import commandHandler.executeCommand.sendFilesExecution
import commandHandler.executeCommand.sendMessageExecution

fun main(args: Array<String>) {
    when (val command = CommandHandler(args = args.toList()).getCommand()) {
        is SendFilesCommand -> sendFilesExecution(command = command)
        is SendMessageCommand -> sendMessageExecution(command = command)
        else -> Unit
    }
}
