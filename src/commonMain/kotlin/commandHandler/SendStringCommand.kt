package commandHandler

import requireNull

object SendStringCommand : SendCommand(), Command {
    private var string: String? = null

    fun getString() = verifyArgumentIsSet(argument = string, reportingName = stringArguments.first())

    override fun verify() {
        super.verify()
        getString()
    }

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (handleSendCommandArgument(argumentName = argumentName, value = value)) {
            return@tryCatch true
        }
        if (stringArguments.contains(argumentName)) {
            requireNull(string)
            string = value
            return@tryCatch true
        }
        return@tryCatch false
    }
}
