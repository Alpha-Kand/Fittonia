package commandHandler

import hmeadowSocket.HMeadowSocketClient
import requireNull
import settingsManager.SettingsManager
import java.net.InetAddress

sealed class SendCommand : Command {
    private var port: String? = null
    private var destination: String? = null
    private var ip: String? = null
    private var password: String? = null

    fun getPort() = verifyArgumentIsSet(argument = port, reportingName = portArguments.first()).toInt()
    fun getDestination() = destination
    fun getIP() = verifyArgumentIsSet(argument = ip, reportingName = ipArguments.first())
    fun getPassword() = verifyArgumentIsSet(argument = password, reportingName = passwordArguments.first())

    override fun verify() {
        if (destination == null) {
            getIP()
            getPassword()
        }
        getPort()
    }

    fun handleSendCommandArgument(argumentName: String, value: String): Boolean {
        if (portArguments.contains(argumentName)) {
            requireNull(port)
            port = value
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
            ipAddress = InetAddress.getByName(destination.ip), // "localhost"),
            port = command.getPort(),
        )
    } ?: HMeadowSocketClient(
        ipAddress = InetAddress.getByName(command.getIP()),
        port = command.getPort(),
    )
}

fun canContinue(command: SendCommand, client: HMeadowSocketClient): Boolean {
    val destination = SettingsManager.settingsManager.findDestination(command.getDestination())
    if (client.receiveConfirmation()) {
        if (client.sendPassword(password = destination?.password ?: command.getPassword())) {
            return true
        }
        println("Server refused password.")
        return false
    }
    println("Connected, but request refused.")
    return false
}
