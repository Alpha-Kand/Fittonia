package commandHandler

import FittoniaError
import FittoniaErrorType
import hmeadowSocket.HMeadowSocket

sealed interface Command {

    fun addArg(argumentName: String, value: String)
    fun verify()

    fun tryCatch(argumentName: String, value: String, addArgBlock: () -> Boolean) {
        try {
            if (addArgBlock()) return
            throw FittoniaError(FittoniaErrorType.COMMAND_DOESNT_TAKE_THIS_ARGUMENT, argumentName)
        } catch (e: IllegalStateException) {
            throw FittoniaError(FittoniaErrorType.DUPLICATE_ARGUMENT, argumentName)
        } catch (e: NumberFormatException) {
            throw FittoniaError(FittoniaErrorType.NON_NUMERICAL_PORT, value)
        }
    }

    companion object {
        fun <T> verifyArgumentIsSet(
            argument: T?,
            reportingName: String,
        ): T = argument ?: throw FittoniaError(FittoniaErrorType.REQUIRED_ARGUMENT_NOT_FOUND, reportingName)
    }
}

fun verifyPortNumber(port: Int?): Boolean {
    if (port != null) {
        val commonReservedPortLimit = 1023 // The first 1023 are highly used/recognized.
        val maxPortNumber = 65536 // 65535 is the highest port number possible (2^16)-1.
        if (port <= commonReservedPortLimit || port >= maxPortNumber) {
            throw FittoniaError(FittoniaErrorType.PORT_NUM_OUT_OF_RANGE, commonReservedPortLimit, maxPortNumber, port)
        }
        return true
    }
    return false
}

fun HMeadowSocket.receiveConfirmation(): Boolean { //TODO replace with sending and receiving booleans.
    return receiveString() == ServerFlagsString.CONFIRM
}
