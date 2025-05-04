import commandHandler.FileTransfer
import fileOperations.FileOperations
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketServer
import java.nio.file.Path
import kotlin.io.path.Path

class DesktopServer private constructor(port: Int) : ServerLogs, Server {
    override var jobId: Int = 100
    override val jobIdMutex = Mutex()
    override val logsMutex = Mutex()

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
                    val newJobId = getAndIncrementJobId()
                    log("Connected to client.", jobId = newJobId)
                    serverCoroutineScope.launch {
                        handleCommand(server, jobId = newJobId)
                    }
                }
            }
        }
    }

    override suspend fun onPing(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        TODO("Not yet implemented") // After release
    }

    override suspend fun onAddDestination(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientAccessCodeSuccess) {
            logWarning("Client attempted to add this server as destination, access code refused.", jobId = jobId)
        } else {
            if (server.receiveBoolean()) {
                log("Client added this server as a destination.", jobId = jobId)
            } else {
                logWarning("Client failed to add this server as a destination.", jobId = jobId)
            }
        }
    }

    override suspend fun onSendFiles(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientAccessCodeSuccess) {
            logWarning("Client attempted to send files to this server, access code refused.", jobId = jobId)
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

    override suspend fun onSendMessage(clientAccessCodeSuccess: Boolean, server: HMeadowSocketServer, jobId: Int) {
        if (!clientAccessCodeSuccess) {
            logWarning("Client attempted to send a message, access code refused.", jobId = jobId)
        } else {
            log("Client message: ${server.receiveString()}", jobId = jobId)
            server.sendConfirmation()
        }
    }

    override suspend fun onInvalidCommand(unknownCommand: String) {
        logWarning("Received invalid server command from client: $unknownCommand", jobId = jobId)
    }

    private suspend fun waitForClient(block: suspend (HMeadowSocketServer) -> Unit) {
        block(HMeadowSocketServer.createServerFromSocket(serverSocket = mainServerSocket))
    }

    override fun HMeadowSocketServer.accessCodeIsValid(): Boolean {
        return SettingsManagerDesktop.settingsManager.checkAccessCode(receiveString())
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

private suspend fun HMeadowSocketServer.getJobName2(flag: String, jobId: Int): String {
    val settingsManager = SettingsManagerDesktop.settingsManager
    val autoJobName = when (flag) {
        ServerFlagsString.NEED_JOB_NAME -> settingsManager.getAutoJobName().also {
            DesktopServer.log("Server generated job name: $it", jobId = jobId)
        }

        ServerFlagsString.HAVE_JOB_NAME -> receiveString().also {
            DesktopServer.log("Client provided job name: $it", jobId = jobId)
        }

        else -> throw Exception() // TODO - After release
    }
    var nonConflictedJobName: String = autoJobName

    var i = 0
    while (FileOperations.exists(Path(path = settingsManager.settings.dumpPath + "/$nonConflictedJobName"))) {
        nonConflictedJobName = autoJobName + "_" + settingsManager.getAutoJobName()
        i++
        if (i > 20) {
            throw Exception() // TODO - After release
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
