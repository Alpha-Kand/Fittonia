package commandHandler

import FittoniaError
import FittoniaErrorType

class CommandHandler(private val args: List<String>) {

    fun getCommand(): Command {
        val enteredCommands = mutableListOf<String>()
        val enteredParameters = mutableListOf<String>()

        args.forEach { arg ->
            commands.find { it == arg }?.let { command ->
                enteredCommands.add(command)
            } ?: enteredParameters.add(arg)
        }

        if (enteredCommands.size > 1 || enteredCommands.isEmpty()) {
            throw FittoniaError(FittoniaErrorType.INVALID_NUM_OF_COMMANDS, enteredCommands.size)
        }

        val command: Command
        try {
            command = when (enteredCommands.first()) {
                addCommand -> AddCommand()
                removeCommand -> RemoveCommand()
                listDestinationsCommand -> ListDestinationsCommand()
                dumpCommand -> DumpCommand()
                serverCommand -> ServerCommand()
                sendFilesCommand -> SendFilesCommand()
                setDefaultPortCommand -> SetDefaultPortCommand()
                serverPasswordCommand -> ServerPasswordCommand()
                sendMessageCommand -> SendMessageCommand()
                exitCommand -> ExitCommand
                else -> throw IllegalArgumentException()
            }
        } catch (_: Exception) {
            throw FittoniaError(FittoniaErrorType.INVALID_NUM_OF_COMMANDS, 0)
        }

        var collectTrailingArgs = false
        val trailingArgs = mutableListOf<String>()

        enteredParameters.forEach { par ->
            if (collectTrailingArgs) {
                trailingArgs.add(par)
            } else if (Regex(pattern = "-{1,2}\\w+=.+").containsMatchIn(par)) { // Passed value.
                command.addArg(
                    argumentName = par.substringBefore(delimiter = "="),
                    value = par.substringAfter(delimiter = "="),
                )
            } else if (Regex(pattern = "-{1,2}\\w+(?<!=)\$").containsMatchIn(par)) { // Flag.
                command.addArg(
                    argumentName = par,
                    value = "",
                )
                if (filesArguments.contains(par) || messageArguments.contains(par)) {
                    collectTrailingArgs = true
                }
            } else {
                throw FittoniaError(FittoniaErrorType.INVALID_ARGUMENT, par)
            }
        }
        if (command is SendFilesCommand) {
            command.setFiles(trailingArgs.toList())
        }
        if (command is SendMessageCommand) {
            command.setMessage(trailingArgs.joinToString(separator = " "))
        }

        command.verify()
        return command
    }
}
