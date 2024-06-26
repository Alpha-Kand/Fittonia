import commandHandler.FileTransfer
import commandHandler.ServerCommandFlag
import commandHandler.ServerCommandFlag.Companion.toCommandFlag
import commandHandler.ServerFlagsString
import fileOperations.FileOperations
import hmeadowSocket.HMeadowSocketServer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path

class DesktopServer private constructor(port: Int) {

    private var jobId: Int = 100

    private val serverCoroutineScope = CoroutineScope(
        context = Dispatchers.IO + CoroutineExceptionHandler { _, e ->
            logError(e.message ?: "Unknown error")
        },
    )

    enum class LogType {
        NORMAL,
        WARNING,
        ERROR,
        DEBUG,
    }

    class Log(
        private val time: ZonedDateTime,
        val message: String,
        val type: LogType,
        val jobId: Int?,
    ) {
        val timeStamp: String = "%1\$s %2\$sh %3\$sm %4\$ss".format(
            time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            time.format(DateTimeFormatter.ofPattern("HH")),
            time.format(DateTimeFormatter.ofPattern("mm")),
            time.format(DateTimeFormatter.ofPattern("ss")),
        )

        constructor(message: String, type: LogType = LogType.NORMAL, jobId: Int? = null) : this(
            time = ZonedDateTime.now(),
            message = message,
            type = type,
            jobId = jobId,
        )
    }

    private val mainServerSocket = HMeadowSocketServer.createServerSocket(port)
    private val mLogs = mutableListOf<Log>()
    fun getLogs(): List<Log> = mLogs.toList()

    private fun start() {
        log("Server started.")
        serverCoroutineScope.launch {
            while (true) {
                log("Waiting for client.")
                waitForClient { server ->
                    val newJobId = synchronized(instance()) {
                        jobId++
                    }
                    log("Connected to client.", jobId = newJobId)
                    serverCoroutineScope.launch {
                        handleCommand(server, jobId = newJobId)
                    }
                }
            }
        }
    }

    fun handleCommand(server: HMeadowSocketServer, jobId: Int) {
        server.handleCommand(
            onAddDestination = ::onAddDestination,
            onSendFilesCommand = ::onSendFiles,
            onSendMessageCommand = ::onSendMessage,
            onInvalidCommand = ::onInvalidCommand,
            jobId = jobId,
        )
    }

    private fun onAddDestination(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientPasswordSuccess) {
            logWarning("Client attempted to add this server as destination, password refused.", jobId = jobId)
        } else {
            if (server.receiveBoolean()) {
                log("Client added this server as a destination.", jobId = jobId)
            } else {
                logWarning("Client failed to add this server as a destination.", jobId = jobId)
            }
        }
    }

    private fun onSendFiles(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientPasswordSuccess) {
            logWarning("Client attempted to send files to this server, password refused.", jobId = jobId)
        } else {
            log("Client attempting to send files.", jobId = jobId)
            val jobPath = server.getJobName2(flag = server.receiveString(), jobId = jobId)
            FileOperations.createDirectory(path = Path(jobPath))
            server.sendInt(jobPath.length)
            if (server.receiveString() == "CANCEL") {
                log("Client cancelled sending files.", jobId = jobId)
                return
            }
            val tempReceivingFolder = FileOperations.createTempDirectory(FileTransfer.tempPrefix)
            val fileTransferCount = server.receiveInt()
            repeat(times = fileTransferCount) {
                server.receiveItemAndReport2(
                    jobPath = jobPath,
                    tempReceivingFolder = tempReceivingFolder,
                    jobId = jobId,
                )
            }
            log("${jobPath.split('/').last()}: $fileTransferCount file(s) received", jobId = jobId)
        }
    }

    private fun onSendMessage(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientPasswordSuccess) {
            logWarning("Client attempted to send a message, password refused.", jobId = jobId)
        } else {
            log("Client message: ${server.receiveString()}", jobId = jobId)
            server.sendConfirmation()
        }
    }

    private fun onInvalidCommand(unknownCommand: String) {
        logWarning("Received invalid server command from client: $unknownCommand", jobId = jobId)
    }

    private fun waitForClient(block: (HMeadowSocketServer) -> Unit) {
        block(HMeadowSocketServer.createServerFromSocket(serverSocket = mainServerSocket))
    }

    companion object {
        private var instance: DesktopServer? = null

        fun instance(): DesktopServer = requireNotNull(instance)
        fun isActive() = instance != null
        fun init(port: Int): Boolean {
            return if (instance == null) {
                instance = DesktopServer(port)
                if (!Config.IS_MOCKING) {
                    instance().start()
                }
                true
            } else {
                false
            }
        }

        fun log(log: String, jobId: Int? = null) = synchronized(instance().mLogs) {
            instance().mLogs.add(Log(log, LogType.NORMAL, jobId))
        }

        fun logWarning(log: String, jobId: Int? = null) = synchronized(instance().mLogs) {
            instance().mLogs.add(Log(log, LogType.WARNING, jobId))
        }

        fun logError(log: String, jobId: Int? = null) = synchronized(instance().mLogs) {
            instance().mLogs.add(Log(log, LogType.ERROR, jobId))
        }

        fun logDebug(log: String, jobId: Int? = null) = synchronized(instance().mLogs) {
            instance().mLogs.add(Log(log, LogType.DEBUG, jobId))
        }
    }
}

private fun HMeadowSocketServer.getJobName2(flag: String, jobId: Int): String {
    val settingsManager = SettingsManager.settingsManager
    val autoJobName = when (flag) {
        ServerFlagsString.NEED_JOB_NAME -> settingsManager.getAutoJobName().also {
            DesktopServer.log("Server generated job name: $it", jobId = jobId)
        }

        ServerFlagsString.HAVE_JOB_NAME -> receiveString().also {
            DesktopServer.log("Client provided job name: $it", jobId = jobId)
        }

        else -> throw Exception() // TODO
    }
    var nonConflictedJobName: String = autoJobName

    var i = 0
    while (FileOperations.exists(Path(path = settingsManager.settings.dumpPath + "/$nonConflictedJobName"))) {
        nonConflictedJobName = autoJobName + "_" + settingsManager.getAutoJobName()
        i++
        if (i > 20) {
            throw Exception() // TODO
        }
    }
    return (settingsManager.settings.dumpPath + "/$nonConflictedJobName").also {
        DesktopServer.log("Sending dumpPath length ${it.length}", jobId = jobId)
    }
}

fun HMeadowSocketServer.receiveItemAndReport2(
    jobPath: String,
    tempReceivingFolder: Path,
    jobId: Int,
) {
    receiveItem(
        jobPath = jobPath,
        tempReceivingFolder = tempReceivingFolder,
        onGetRelativePath = { relativePath ->
            DesktopServer.log("Received: $relativePath", jobId = jobId)
        },
        onDone = { },
    )
}

fun HMeadowSocketServer.receiveItem(
    jobPath: String,
    tempReceivingFolder: Path,
    onGetRelativePath: (String) -> Unit,
    onDone: () -> Unit,
) {
    val relativePath = receiveString()
    onGetRelativePath(relativePath)
    val destinationPath = "$jobPath/$relativePath"
    if (receiveBoolean()) { // Is a file.
        val (tempFile, _) = receiveFile(
            destination = "$tempReceivingFolder/",
            prefix = FileTransfer.tempPrefix,
            suffix = FileTransfer.tempSuffix,
        )
        FileOperations.move(source = Path(tempFile), destination = Path(destinationPath))
    } else {
        FileOperations.createDirectory(path = Path(destinationPath))
    }
    sendContinue()
    onDone()
}

private fun HMeadowSocketServer.passwordIsValid() = SettingsManager.settingsManager.checkPassword(receiveString())

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
