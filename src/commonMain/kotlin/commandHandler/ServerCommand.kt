package commandHandler

import requireNull

object ServerCommand : Command() {
    private var port: String? = null

    fun getPort() = verifyArgumentIsSet(argument = port, reportingName = portArguments.first()).toInt()

    override fun verify() {
        getPort()
    }

    override fun addArg(argumentName: String, value: String) {
        try {
            if (portArguments.contains(argumentName)) {
                requireNull(port)
                port = value
                return
            }
            throw IllegalArgumentException("This command does not take this argument: $argumentName")
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Duplicate argument found: $argumentName")
        }
    }
}
