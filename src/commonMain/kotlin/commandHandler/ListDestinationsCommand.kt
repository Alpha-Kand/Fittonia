package commandHandler

import requireNull

object ListDestinationsCommand : Command {
    private var name: String? = null

    fun getName() = name

    override fun verify() {}

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (nameArguments.contains(argumentName)) {
            requireNull(name)
            name = value
            return@tryCatch true
        }
        return@tryCatch false
    }
}
