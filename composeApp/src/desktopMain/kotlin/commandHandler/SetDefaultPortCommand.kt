package commandHandler

import FittoniaError
import FittoniaErrorType
import requireNull

class SetDefaultPortCommand : Command() {
    private var port: Int? = null
    private var clear: Boolean = false

    fun getPort() = port
    fun getClear() = clear

    override fun verify() {
        if (verifyPortNumber(port) && clear) {
            throw FittoniaError(FittoniaErrorType.SET_AND_RESET_DEFAULT_PORT)
        }
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

        if (clearArguments.contains(argumentName)) {
            if (value.isEmpty()) {
                clear = true
                return@tryCatch true
            } else {
                throw FittoniaError(FittoniaErrorType.ARGUMENT_DOESNT_TAKE_VALUE, argumentName)
            }
        }

        return@tryCatch false
    }
}
