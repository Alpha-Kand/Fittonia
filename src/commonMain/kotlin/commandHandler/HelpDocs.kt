package commandHandler

import commandHandler.executeCommand.helpExecution

class HasHelpedException : Exception()

interface HelpDocs {
    var hasHelped: Boolean

    fun defaultHelped() = false

    fun showHelp(argument: String, commandName: String): Boolean {
        if (helpArguments.contains(argument)) {
            helpExecution(
                command = HelpCommand().also {
                    it.addArg(searchArguments.first(), commandName)
                },
            )
            hasHelped = true
            return true
        }
        return false
    }
}
