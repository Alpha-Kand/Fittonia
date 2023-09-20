import commandHandler.AddCommand
import commandHandler.CommandHandler
import commandHandler.DumpCommand
import commandHandler.ExitCommand
import commandHandler.ListDestinationsCommand
import commandHandler.RemoveCommand
import commandHandler.SendFilesCommand
import commandHandler.SendMessageCommand
import commandHandler.ServerCommand
import commandHandler.ServerPasswordCommand
import commandHandler.SetDefaultPortCommand
import commandHandler.executeCommand.addExecution
import commandHandler.executeCommand.dumpExecution
import commandHandler.executeCommand.listDestinationsExecution
import commandHandler.executeCommand.removeExecution
import commandHandler.executeCommand.serverExecution
import commandHandler.executeCommand.serverPasswordExecution
import commandHandler.executeCommand.setDefaultPortExecution
import hmeadowSocket.HMeadowSocket
import settingsManager.SettingsManager
import java.io.File
import java.util.Scanner
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    SettingsManager.settingsManager.saveSettings()
    if (args.isNotEmpty()) {
        handleArguments(args.toList())
    }
    val scanner = Scanner(System.`in`)
    do {
        print("> ")
        val input = scanner.nextLine()?.split(" ") ?: return
        val shouldContinue = handleArguments(input)
    } while (shouldContinue)

    return

    try {
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

fun handleArguments(input: List<String>): Boolean {
    when (val command = CommandHandler(args = input).getCommand()) {
        is AddCommand -> addExecution(command = command)
        is RemoveCommand -> removeExecution(command = command)
        is ListDestinationsCommand -> listDestinationsExecution(command = command)
        is DumpCommand -> dumpExecution(command = command)
        is ServerCommand -> serverExecution(command = command)
        is SendFilesCommand,
        is SendMessageCommand,
        -> {
            val currentDirectory = System.getProperty("user.dir")
            val clientEngineCmdLine = StringBuilder()
                .append("java -jar $currentDirectory/build/compose/jars/FittoniaClientEngine-linux-x64-1.0.jar")
            input.forEach {
                clientEngineCmdLine.append(' ')
                clientEngineCmdLine.append(it)
            }
            ProcessBuilder(*clientEngineCmdLine.toString().split(' ').toTypedArray())
                .directory(File(currentDirectory))
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor(60, TimeUnit.MINUTES)
        }

        is SetDefaultPortCommand -> setDefaultPortExecution(command = command)
        is ServerPasswordCommand -> serverPasswordExecution(command = command)
        is ExitCommand -> return false
        else -> throw IllegalStateException("No valid command detected.")
    }
    return true
}
