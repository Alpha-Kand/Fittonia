package commandHandler

import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketServer
import settingsManager.SettingsManager
import java.lang.NumberFormatException

sealed interface Command {

    fun addArg(argumentName: String, value: String)
    fun verify()

    fun <T> verifyArgumentIsSet(
        argument: T?,
        reportingName: String,
    ): T = requireNotNull(argument) { "Required argument was not found: $reportingName" }

    fun tryCatch(argumentName: String, value: String, addArgBlock: () -> Boolean) {
        try {
            if (addArgBlock()) return

            throw IllegalArgumentException("This command does not take this argument: $argumentName")
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Duplicate argument found: $argumentName")
        } catch (e: NumberFormatException) {
            throw IllegalStateException("Non-numerical port: $value")
        }
    }
}

fun verifyPortNumber(port: Int?): Boolean {
    if (port != null) {
        val commonReservedPortLimit = 1024
        val maxPortNumber = 65535
        if (port < commonReservedPortLimit || port > maxPortNumber) {
            throw IllegalArgumentException(
                "Given port out of range ($commonReservedPortLimit-$maxPortNumber): $port",
            )
        }
        return true
    }
    return false
}

fun HMeadowSocketClient.sendPassword(password: String): Boolean {
    sendString(password)
    return receiveConfirmation()
}

fun HMeadowSocketServer.receivePassword(): Boolean {
    return if (SettingsManager.settingsManager.checkPassword(receiveString())) {
        sendConfirmation()
        true
    } else {
        sendDeny()
        false
    }
}

fun HMeadowSocket.receiveConfirmation(): Boolean {
    return receiveInt() == ServerFlags.CONFIRM
}

fun HMeadowSocket.sendConfirmation() {
    sendInt(ServerFlags.CONFIRM)
}

fun HMeadowSocket.sendDeny() {
    sendInt(ServerFlags.DENY)
}
