package commandHandler

import FittoniaError
import FittoniaErrorType
import ServerFlagsString
import SessionManager
import decodeIpAddress
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocket
import requireNull

sealed class Command {
    val machineReadableOutput = MachineReadableOutput()

    abstract suspend fun addArg(argumentName: String, value: String)
    abstract fun verify()

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

fun HMeadowSocket.receiveConfirmation(): Boolean { // TODO replace with sending and receiving booleans. - After release
    return receiveString() == ServerFlagsString.CONFIRM && receiveBoolean()
}

sealed class SendCommand : Command() {
    private var port: Int? = null
    private var destination: String? = null
    private var ip: String? = null
    private var accessCode: String? = null

    open fun getDestination() = destination
    fun getPort() = verifyArgumentIsSet(argument = port, reportingName = portArguments.first())
    fun getIP() = verifyArgumentIsSet(argument = ip, reportingName = ipArguments.first())
    fun getAccessCode() = verifyArgumentIsSet(argument = accessCode, reportingName = accessCodeArguments.first())

    override fun verify() {
        if (destination == null) {
            // getIP() TODO - After release
            getAccessCode()
        }
        verifyPortNumber(port)
    }

    fun setFromSession() {
        SessionManager.port?.let { port = it }
        SessionManager.destination?.let { destination = it }
        SessionManager.ip?.let { ip = it }
        SessionManager.accessCode?.let { accessCode = it }
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
        if (accessCodeArguments.contains(argumentName)) {
            requireNull(accessCode)
            accessCode = value
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
