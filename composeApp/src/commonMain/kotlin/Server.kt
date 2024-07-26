import ServerCommandFlag.Companion.toCommandFlag
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketServer

interface Server {
    fun HMeadowSocketServer.passwordIsValid(): Boolean

    fun HMeadowSocketServer.handleCommand(
        onSendFilesCommand: (Boolean, HMeadowSocketServer, Int) -> Unit,
        onSendMessageCommand: (Boolean, HMeadowSocketServer, Int) -> Unit,
        onAddDestination: (Boolean, HMeadowSocketServer, Int) -> Unit,
        onInvalidCommand: (String) -> Unit,
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
}
