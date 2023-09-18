package commandHandler

import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketServer
import settingsManager.SettingsManager
import java.lang.NumberFormatException

class CommandHandler(private val args: Array<String>) {

    private val commands = listOf(
        addCommand,
        removeCommand,
        dumpCommand,
        listDestinationsCommand,
        sendFilesCommand,
        serverCommand,
        setDefaultPortCommand,
        serverPasswordCommand,
        "terminal",
    )

    fun getCommand(): Command {
        val enteredCommands = mutableListOf<String>()
        val enteredParameters = mutableListOf<String>()

        args.forEach { arg ->
            commands.find { it == arg }?.let { command ->
                enteredCommands.add(command)
            } ?: enteredParameters.add(arg)
        }

        if (enteredCommands.size > 1 || enteredCommands.isEmpty()) {
            throw IllegalStateException("Invalid number of commands detected: " + enteredCommands.size.toString())
        }

        val command = when (enteredCommands.first()) {
            addCommand -> AddCommand
            removeCommand -> RemoveCommand
            listDestinationsCommand -> ListDestinationsCommand
            dumpCommand -> DumpCommand
            serverCommand -> ServerCommand
            sendFilesCommand -> SendFilesCommand
            setDefaultPortCommand -> SetDefaultPortCommand
            serverPasswordCommand -> ServerPasswordCommand
            else -> throw IllegalArgumentException()
        }

        var collectSources = false
        val sourceList = mutableListOf<String>()

        enteredParameters.forEach { par ->
            if (collectSources) {
                sourceList.add(par)
            } else if (Regex(pattern = "-{1,2}\\w+=.+").containsMatchIn(par)) { // Passed value.
                command.addArg(
                    argumentName = par.substringBefore(delimiter = "="),
                    value = par.substringAfter(delimiter = "="),
                )
            } else if (Regex(pattern = "-{1,2}\\w+(?<!=)\$").containsMatchIn(par)) { // Flag.
                val arg = par.substringBefore(delimiter = "=")
                command.addArg(
                    argumentName = arg,
                    value = "",
                )
                if (filesArguments.contains(arg)) {
                    collectSources = true
                }
            } else {
                throw IllegalArgumentException("Invalid parameter: $par")
            }
        }
        if (command is SendFilesCommand) {
            command.setFiles(sourceList.toList())
        }

        command.verify()
        return command
    }
}
