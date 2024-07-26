import commandHandler.FileTransfer
import fileOperations.FileOperations
import hmeadowSocket.HMeadowSocketServer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Path
import kotlin.io.path.Path

class DesktopServer private constructor(port: Int) : ServerLogs, Server {
    override var jobId: Int = 100

    private val serverCoroutineScope = CoroutineScope(
        context = Dispatchers.IO + CoroutineExceptionHandler { _, e ->
            logError(e.message ?: "Unknown error")
        },
    )

    private val mainServerSocket = HMeadowSocketServer.createServerSocket(port)
    override val mLogs = mutableListOf<Log>()
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

    override fun onAddDestination(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
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

    override fun onSendFiles(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
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

    override fun onSendMessage(clientPasswordSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientPasswordSuccess) {
            logWarning("Client attempted to send a message, password refused.", jobId = jobId)
        } else {
            log("Client message: ${server.receiveString()}", jobId = jobId)
            server.sendConfirmation()
        }
    }

    override fun onInvalidCommand(unknownCommand: String) {
        logWarning("Received invalid server command from client: $unknownCommand", jobId = jobId)
    }

    private fun waitForClient(block: (HMeadowSocketServer) -> Unit) {
        block(HMeadowSocketServer.createServerFromSocket(serverSocket = mainServerSocket))
    }

    override fun HMeadowSocketServer.passwordIsValid(): Boolean {
        return SettingsManagerDesktop.settingsManager.checkPassword(receiveString())
    }

    companion object {
        private var instance: DesktopServer? = null

        fun instance(): DesktopServer = requireNotNull(instance)
        fun isActive() = instance != null
        fun init(port: Int): Boolean {
            return if (instance == null) {
                instance = DesktopServer(port)
                if (!MockConfig.IS_MOCKING) {
                    instance().start()
                }
                true
            } else {
                false
            }
        }

        fun log(log: String, jobId: Int? = null) = instance().log(log, jobId)
        fun logWarning(log: String, jobId: Int? = null) = instance().logWarning(log, jobId)
        fun logError(log: String, jobId: Int? = null) = instance().logError(log, jobId)
        fun logDebug(log: String, jobId: Int? = null) = instance().logDebug(log, jobId)
    }
}

private fun HMeadowSocketServer.getJobName2(flag: String, jobId: Int): String {
    val settingsManager = SettingsManagerDesktop.settingsManager
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
