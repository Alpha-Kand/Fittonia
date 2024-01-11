package commandHandler

import SessionManager
import commandHandler.Command.Companion.verifyArgumentIsSet
import hmeadowSocket.HMeadowSocketClient
import requireNull
import settingsManager.SettingsManager
import java.net.InetAddress

sealed class SendCommand : Command {
    private var port: Int? = null
    private var destination: String? = null
    private var ip: String? = null
    private var password: String? = null

    open fun getDestination() = destination
    fun getPort() = verifyArgumentIsSet(argument = port, reportingName = portArguments.first())
    fun getDestination() = destination
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
            ip = value
            return true
        }
        return false
    }
}

fun setupSendCommandClient(command: SendCommand): HMeadowSocketClient {
    val destination = SettingsManager.settingsManager.findDestination(command.getDestination())
    return destination?.let {
        HMeadowSocketClient(
            ipAddress = InetAddress.getByName(destination.ip),
            port = command.getPort(),
            timeoutMillis = 2000L,
        )
    } ?: HMeadowSocketClient(
        ipAddress = InetAddress.getByName(command.getIP()),
        port = command.getPort(),
        timeoutMillis = 2000L,
    )
}

fun canContinue(command: SendCommand, client: HMeadowSocketClient, parent: HMeadowSocketClient): Boolean {
    val destination = SettingsManager.settingsManager.findDestination(command.getDestination())
    if (client.receiveConfirmation()) {
        if (client.sendPassword(password = destination?.password ?: command.getPassword())) {
            return true
        }
        parent.sendInt(ServerFlags.HAS_MORE)
        parent.sendString("Server refused password.")
        return false
    }
    parent.sendInt(ServerFlags.HAS_MORE)
    parent.sendString("Connected, but request refused.")
    return false
}
