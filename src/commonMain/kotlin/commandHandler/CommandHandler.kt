package commandHandler

import java.util.Locale

class CommandHandler(private val args: Array<String>) {

    private val commands = listOf(addCommand, removeCommand, dumpCommand, listDestinationsCommand, "send", "server", "terminal")

    fun getCommand(): Command {
        val enteredCommands = mutableListOf<String>()
        val enteredParameters = mutableListOf<String>()

        args.forEach { arg ->
            val lowerCaseArg = arg.lowercase(locale = Locale.ROOT)
            commands.find { it == lowerCaseArg }?.let { command ->
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
            else -> throw IllegalArgumentException()
        }

        enteredParameters.forEach { par ->
            if (Regex(pattern = "\\w+=.+").containsMatchIn(par)) {
                command.addArg(
                    argumentName = par.substringBefore(delimiter = "=").lowercase(locale = Locale.ROOT),
                    value = par.substringAfter(delimiter = "="),
                )
            } else {
                throw IllegalArgumentException("Invalid parameter: $par")
            }
        }
        command.verify()
        return command
    }
}

sealed class Command {

    abstract fun addArg(argumentName: String, value: String)
    abstract fun verify()

    fun verifyArgumentIsSet(
        argument: String?,
        reportingName: String,
    ): String = requireNotNull(argument) { "Required argument was not found: $reportingName" }
}
