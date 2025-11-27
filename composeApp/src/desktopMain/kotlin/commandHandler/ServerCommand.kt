package commandHandler

import commandHandler.Command.Companion.verifyArgumentIsSet
import requireNull

class ServerCommand : Command() {
    private var port: Int? = null

    fun getPort() = verifyArgumentIsSet(argument = port, reportingName = portArguments.first())

    override fun verify() {
        getPort()
    }

    override suspend fun addArg(
        argumentName: String,
        value: String,
    ) = tryCatch(argumentName = argumentName, value = value) {
        if (portArguments.contains(argumentName)) {
            requireNull(port)
            port = value.toInt()
            return@tryCatch true
        }
        return@tryCatch false
    }
}
