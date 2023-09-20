package commandHandler

import requireNull

class RemoveCommand : Command {
    private var name: String? = null

    fun getName() = verifyArgumentIsSet(argument = name, reportingName = nameArguments.first())

    override fun verify() {
        getName()
    }

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (nameArguments.contains(argumentName)) {
            requireNull(name)
            name = value
            return@tryCatch true
        }
        return@tryCatch false
    }
}
