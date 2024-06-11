import commandHandler.executeCommand.handleCommand
import hmeadowSocket.HMeadowSocketServer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LocalServer private constructor(port: Int) {

    private val serverCoroutineScope = CoroutineScope(
        context = Dispatchers.IO + CoroutineExceptionHandler { _, e ->
            logError(e.message ?: "Unknown error")
        },
    )

    enum class LogType {
        NORMAL,
        WARNING,
        ERROR,
    }

    class Log(
        private val time: ZonedDateTime,
        val message: String,
        val type: LogType,
    ) {
        val timeStamp: String = "%1\$s %2\$sh %3\$sm %4\$ss".format(
            time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            time.format(DateTimeFormatter.ofPattern("HH")),
            time.format(DateTimeFormatter.ofPattern("mm")),
            time.format(DateTimeFormatter.ofPattern("ss")),
        )

        constructor(message: String, type: LogType = LogType.NORMAL) : this(
            time = ZonedDateTime.now(),
            message = message,
            type = type,
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
                    log("Connected to client.")
                    serverCoroutineScope.launch {
                        server.handleCommand(
                            onAddDestination = ::onAddDestination,
                            onSendFilesCommand = ::onSendFiles,
                            onSendMessageCommand = ::onSendMessage,
                            onInvalidCommand = ::onInvalidCommand,
                        )
                    }
                }
            }
        }
    }

    private fun onAddDestination(clientPasswordSuccess: Boolean, server: HMeadowSocketServer) {
        if (!clientPasswordSuccess) {
            logWarning("Client attempted to add this server as destination, password refused.")
        } else {
            if (server.receiveBoolean()) {
                log("Client added this server as a destination.")
            } else {
                logWarning("Client failed to add this server as a destination.")
            }
        }
    }

    private fun onSendFiles(clientPasswordSuccess: Boolean, server: HMeadowSocketServer) { /* TODO */
    }

    private fun onSendMessage(clientPasswordSuccess: Boolean, server: HMeadowSocketServer) { /* TODO */
    }

    private fun onInvalidCommand(unknownCommand: String) {
        logWarning("Received invalid server command from client: $unknownCommand")
    }

    private fun waitForClient(block: (HMeadowSocketServer) -> Unit) {
        block(HMeadowSocketServer.createServerFromSocket(serverSocket = mainServerSocket))
    }

    companion object {
        private var instance: LocalServer? = null

        fun instance(): LocalServer = requireNotNull(instance)
        fun isActive() = instance != null
        fun init(port: Int): Boolean {
            return if (instance == null) {
                instance = LocalServer(port)
                instance().start()
                true
            } else {
                false
            }
        }

        fun log(log: String) = synchronized(instance()) {
            instance().mLogs.add(Log(log))
        }

        fun logWarning(log: String) = synchronized(instance()) {
            instance().mLogs.add(Log(log, LogType.WARNING))
        }

        fun logError(log: String) = synchronized(instance()) {
            instance().mLogs.add(Log(log, LogType.ERROR))
        }
    }
}
