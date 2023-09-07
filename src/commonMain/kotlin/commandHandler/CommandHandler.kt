package commandHandler

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
            else -> throw IllegalArgumentException()
        }

        enteredParameters.forEach { par ->
            if (Regex(pattern = "-{1,2}\\w+=.+").containsMatchIn(par)) { // Passed value.
                command.addArg(
                    argumentName = par.substringBefore(delimiter = "="),
                    value = par.substringAfter(delimiter = "="),
                )
            } else if(Regex(pattern = "-{1,2}\\w+(?<!=)\$").containsMatchIn(par)) { // Flag.
                command.addArg(
                    argumentName = par.substringBefore(delimiter = "="),
                    value = "",
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

    fun <T> verifyArgumentIsSet(
        argument: T?,
        reportingName: String,
    ): T = requireNotNull(argument) { "Required argument was not found: $reportingName" }

    fun tryCatch(argumentName: String, value:String, addArgBlock: () -> Boolean) {
        try {
            if(addArgBlock()) return

            throw IllegalArgumentException("This command does not take this argument: $argumentName")
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Duplicate argument found: $argumentName")
        } catch (e: NumberFormatException) {
            throw IllegalStateException("Non-numerical port: $value")
        }
    }
}

fun verifyPortNumber(port: Int?):Boolean {
    if(port != null) {
        val commonReservedPortLimit = 1024
        val maxPortNumber = 65535
        if (port < commonReservedPortLimit || port > maxPortNumber) {
            throw IllegalArgumentException(
                "Given port out of range ($commonReservedPortLimit-$maxPortNumber): $port",
            )
        }
        return true
    }
    return false
}