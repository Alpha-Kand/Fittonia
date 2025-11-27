import ServerCommandFlag.Companion.toCommandFlag
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocket
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketClient
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketServer

interface Server {
    var jobId: Int
    val jobIdMutex: Mutex

    suspend fun getAndIncrementJobId(): Int {
        return jobIdMutex.withLock {
            val id = jobId
            jobId++
            id
        }
    }

    fun HMeadowSocketServer.accessCodeIsValid(): Boolean

    suspend fun HMeadowSocketServer.handleCommand(
        onSendFilesCommand: suspend (Boolean, HMeadowSocketServer, Int) -> Unit,
        onSendMessageCommand: suspend (Boolean, HMeadowSocketServer, Int) -> Unit,
        onAddDestination: suspend (Boolean, HMeadowSocketServer, Int) -> Unit,
        onPing: suspend (Boolean, HMeadowSocketServer, Int) -> Unit,
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
        val accessCodeIsValid = accessCodeIsValid()
        sendApproval(choice = accessCodeIsValid)
        when (command) {
            ServerCommandFlag.PING -> onPing(accessCodeIsValid, this, jobId)
            ServerCommandFlag.SEND_FILES -> onSendFilesCommand(accessCodeIsValid, this, jobId)
            ServerCommandFlag.SEND_MESSAGE -> onSendMessageCommand(accessCodeIsValid, this, jobId)
            ServerCommandFlag.ADD_DESTINATION -> onAddDestination(accessCodeIsValid, this, jobId)
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
            jobId = jobId,
            onPing = ::onPing,
            onInvalidCommand = ::onInvalidCommand,
            onAddDestination = ::onAddDestination,
            onSendFilesCommand = ::onSendFiles,
            onSendMessageCommand = ::onSendMessage,
        )
    }

    suspend fun onPing(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int)
    suspend fun onSendFiles(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int)
    suspend fun onSendMessage(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int)
    suspend fun onAddDestination(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int)
    suspend fun onInvalidCommand(unknownCommand: String)
}

// TODO Sending files should be handled in Server. - After release
fun <T> HMeadowSocket.receiveApproval(onConfirm: () -> T, onDeny: () -> T): T {
    receiveString()
    return when (receiveBoolean()) {
        true -> onConfirm()
        false -> onDeny()
    }
}

// TODO Sending files should be handled in Server. - After release
fun <T> HMeadowSocketClient.communicateCommand(
    commandFlag: ServerCommandFlag,
    accessCode: String,
    onSuccess: () -> T,
    onAccessCodeRefused: () -> T,
    onFailure: () -> T,
): T {
    sendString(message = commandFlag.text)
    return receiveApproval(
        onConfirm = {
            sendString(accessCode)
            receiveApproval(
                onConfirm = { onSuccess() },
                onDeny = { onAccessCodeRefused() },
            )
        },
        onDeny = { onFailure() },
    )
}

// TODO Sending files should be handled in Server. - After release
fun HMeadowSocketClient.communicateCommandBoolean(
    commandFlag: ServerCommandFlag,
    accessCode: String,
    onSuccess: () -> Unit,
    onAccessCodeRefused: () -> Unit,
    onFailure: () -> Unit,
): Boolean {
    sendString(message = commandFlag.text)
    return receiveApproval(
        onConfirm = {
            sendString(accessCode)
            receiveApproval(
                onConfirm = {
                    onSuccess()
                    true
                },
                onDeny = {
                    onAccessCodeRefused()
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
