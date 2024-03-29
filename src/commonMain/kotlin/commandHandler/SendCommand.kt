package commandHandler

import SessionManager
import commandHandler.Command.Companion.verifyArgumentIsSet
import hmeadowSocket.HMeadowSocketClient
import receiveApproval
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
        if (clientEnginePortArguments.contains(argumentName)) {
            return true
        }
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

fun HMeadowSocketClient.communicateCommand(
    commandFlag: ServerCommandFlag,
    password: String,
    onSuccess: () -> Unit,
    onPasswordRefused: () -> Unit,
    onFailure: () -> Unit,
): Boolean {
    sendString(message = commandFlag.text)
    return receiveApproval(
        onConfirm = {
            sendString(password)
            receiveApproval(
                onConfirm = {
                    onSuccess()
                    true
                },
                onDeny = {
                    onPasswordRefused()
                    false
                },
            )
        },
        onDeny = {
            onFailure()
            false
        },
    )
}

fun canContinueSendCommand(command: SendCommand, client: HMeadowSocketClient, parent: HMeadowSocketClient): Boolean {
    val destination = SettingsManager.settingsManager.findDestination(command.getDestination())
    val password = destination?.password ?: command.getPassword()
    val commandFlag = when (command) {
        is SendFilesCommand -> ServerCommandFlag.SEND_FILES
        is SendMessageCommand -> ServerCommandFlag.SEND_MESSAGE
        is AddCommand -> ServerCommandFlag.ADD_DESTINATION
    }
    return client.communicateCommand(
        commandFlag = commandFlag,
        password = password,
        onSuccess = {
            parent.sendString(ServerFlagsString.HAS_MORE)
            parent.sendString("Server accepted password.")
        },
        onPasswordRefused = {
            parent.sendString(ServerFlagsString.HAS_MORE)
            parent.sendString("Server refused password.")
        },
        onFailure = {
            parent.sendString(ServerFlagsString.HAS_MORE)
            parent.sendString("Connected, but request refused.")
        },
    )
}
