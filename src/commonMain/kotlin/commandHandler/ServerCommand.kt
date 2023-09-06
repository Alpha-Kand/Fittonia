package commandHandler

import requireNull

object ServerCommand : Command() {
    private var port: String? = null

    fun getPort() = verifyArgumentIsSet(argument = port, reportingName = portArguments.first()).toInt()

    override fun verify() {
        getPort()
    }

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (portArguments.contains(argumentName)) {
            requireNull(port)
            port = value
            return@tryCatch true
        }
        return@tryCatch false
    }
}
