import ServerCommandFlag.Companion.toCommandFlag
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketServer

interface Server {
    var jobId: Int

    fun HMeadowSocketServer.passwordIsValid(): Boolean

    suspend fun HMeadowSocketServer.handleCommand(
        onSendFilesCommand: suspend (Boolean, HMeadowSocketServer, Int) -> Unit,
        onSendMessageCommand: suspend (Boolean, HMeadowSocketServer, Int) -> Unit,
        onAddDestination: suspend (Boolean, HMeadowSocketServer, Int) -> Unit,
        onInvalidCommand: suspend (String) -> Unit,
        jobId: Int,
    ) {
        val receivedCommand = receiveString()
        val command: ServerCommandFlag
        try {
            command = requireNotNull(receivedCommand.toCommandFlag())
        } catch (e: Exception) {
            sendDeny()
            onInvalidCommand(receivedCommand)
            return
        }
        sendConfirmation()
        val passwordIsValid = passwordIsValid()
        sendApproval(choice = passwordIsValid)
        when (command) {
            ServerCommandFlag.SEND_FILES -> onSendFilesCommand(passwordIsValid, this, jobId)
            ServerCommandFlag.SEND_MESSAGE -> onSendMessageCommand(passwordIsValid, this, jobId)
            ServerCommandFlag.ADD_DESTINATION -> onAddDestination(passwordIsValid, this, jobId)
        }
    }

    fun HMeadowSocket.sendDeny() {
        sendString(ServerFlagsString.DENY)
        sendBoolean(false)
    }

    fun HMeadowSocket.sendConfirmation() {
        sendString(ServerFlagsString.CONFIRM)
        sendBoolean(true)
    }

    fun HMeadowSocket.sendApproval(choice: Boolean) {
        if (choice) {
            sendConfirmation()
        } else {
            sendDeny()
        }
    }

    suspend fun handleCommand(server: HMeadowSocketServer, jobId: Int) {
        server.handleCommand(
            onAddDestination = ::onAddDestination,
            onSendFilesCommand = ::onSendFiles,
            onSendMessageCommand = ::onSendMessage,
            onInvalidCommand = ::onInvalidCommand,
            jobId = jobId,
        )
    }

    suspend fun onAddDestination(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int)
    suspend fun onSendFiles(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int)
    suspend fun onSendMessage(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int)
    suspend fun onInvalidCommand(unknownCommand: String)
}

// TODO Sending files should be handled in Server.
fun <T> HMeadowSocket.receiveApproval(onConfirm: () -> T, onDeny: () -> T): T {
    receiveString()
    return when (receiveBoolean()) {
        true -> onConfirm()
        false -> onDeny()
    }
}

// TODO Sending files should be handled in Server.
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
