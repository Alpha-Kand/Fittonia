package commandHandler

import FittoniaError
import FittoniaErrorType
import SessionManager
import commandHandler.Command.Companion.verifyArgumentIsSet
import decodeIpAddress
import hmeadowSocket.HMeadowSocket
import requireNull

sealed interface Command {

    suspend fun addArg(argumentName: String, value: String)
    fun verify()

    suspend fun tryCatch(argumentName: String, value: String, addArgBlock: suspend () -> Boolean) {
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

fun HMeadowSocket.receiveConfirmation(): Boolean { // TODO replace with sending and receiving booleans.
    return receiveString() == ServerFlagsString.CONFIRM && receiveBoolean()
}

sealed class SendCommand : Command {
    private var port: Int? = null
    private var destination: String? = null
    private var ip: String? = null
    private var password: String? = null

    open fun getDestination() = destination
    fun getPort() = verifyArgumentIsSet(argument = port, reportingName = portArguments.first())
    fun getIP() = verifyArgumentIsSet(argument = ip, reportingName = ipArguments.first())
    fun getPassword() = verifyArgumentIsSet(argument = password, reportingName = passwordArguments.first())

    override fun verify() {
        if (destination == null) {
            // getIP() TODO
            getPassword()
        }
        verifyPortNumber(port)
    }

    fun setFromSession() {
        SessionManager.port?.let { port = it }
        SessionManager.destination?.let { destination = it }
        SessionManager.ip?.let { ip = it }
        SessionManager.password?.let { password = it }
    }

    fun handleSendCommandArgument(argumentName: String, value: String): Boolean {
        if (portArguments.contains(argumentName)) {
            requireNull(port)
            port = value.toInt()
            return true
        }
        if (destinationArguments.contains(argumentName)) {
            requireNull(destination)
            destination = value
            return true
        }
        if (passwordArguments.contains(argumentName)) {
            requireNull(password)
            password = value
            return true
        }
        if (ipArguments.contains(argumentName)) {
            requireNull(ip)
            ip = try {
                decodeIpAddress(value)
            } catch (e: Exception) {
                null
            } ?: value
            return true
        }
        return false
    }
}
