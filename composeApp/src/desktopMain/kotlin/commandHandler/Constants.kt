package commandHandler

const val addCommand = "add"
const val removeCommand = "remove"
const val listDestinationsCommand = "list"
const val dumpCommand = "dump"
const val serverCommand = "server"
const val sendFilesCommand = "send"
const val setDefaultPortCommand = "default-port"
const val serverPasswordCommand = "server-password"
const val sendMessageCommand = "send-message"
const val ipCodeCommand = "ip-code"
const val decodeIpCodeCommand = "decode-ip-code"
const val exitCommand = "exit"
const val quitCommand = "quit"
const val sessionCommand = "session"
const val logsCommand = "logs"
const val helpCommand = "help"

val commands = listOf(
    addCommand,
    removeCommand,
    dumpCommand,
    listDestinationsCommand,
    sendFilesCommand,
    serverCommand,
    setDefaultPortCommand,
    serverPasswordCommand,
    sendMessageCommand,
    ipCodeCommand,
    decodeIpCodeCommand,
    exitCommand,
    quitCommand,
    sessionCommand,
    logsCommand,
    helpCommand,
)

val nameArguments = listOf("--name", "-n")
val ipArguments = listOf("--ip", "-i")
val passwordArguments = listOf("--password", "-P", "--pw")
val pathArguments = listOf("--path", "--dump", "-D")
val portArguments = listOf("--port", "-p")
val destinationArguments = listOf("--destination", "-d", "--dest")
val filesArguments = listOf("--files", "-f", "--source", "-s")
val clearArguments = listOf("--clear")
val newArguments = listOf("--new")
val oldArguments = listOf("--old")
val jobArguments = listOf("--job", "-j")
val messageArguments = listOf("--message", "--string")
val sessionArguments = listOf("--session", "-e")
val ipCodeArguments = listOf("--ipcode")
val machineReadableOutputArguments = listOf("--ioformat", "-z")
val searchCommandsArguments = listOf("--command", "-c")
val searchArguments = listOf("--search", "-s")
val helpArguments = listOf("--help", "-h")

class ServerFlagsString {
    companion object {
        const val CONFIRM = "CONFIRM"
        const val DENY = "DENY"

        const val SHARE_JOB_NAME = "SHARE_JOB_NAME"
        const val RECEIVING_ITEM = "RECEIVING_ITEM"
        const val HAVE_JOB_NAME = "HAVE_JOB_NAME"
        const val NEED_JOB_NAME = "NEED_JOB_NAME"
        const val DONE = "DONE"

        const val HAS_MORE = "HAS_MORE"
        const val PRINT_LINE = "PRINT_LINE"
        const val FILE_NAMES_TOO_LONG = "FILE_NAMES_TOO_LONG"
        const val SEND_FILES_COLLECTING = "SEND_FILES_COLLECTING"
        const val ADD_DESTINATION = "ADD_DESTINATION"
    }
}

enum class ServerCommandFlag(val text: String) {
    SEND_FILES(text = "SEND_FILES"),
    SEND_MESSAGE(text = "SEND_MESSAGE"),
    ADD_DESTINATION(text = "ADD_DESTINATION"),
    ;

    companion object {
        fun String.toCommandFlag() = entries.find { it.text == this }
    }
}

class FileTransfer {
    companion object {
        const val tempPrefix = "fittonia"
        const val tempSuffix = ".fittonia"

        private const val prefixLength = 2
        const val filePrefix = "F?"
        const val dirPrefix = "D?"

        const val NORMAL = 0
        const val CANCEL = 1
        const val SKIP_INVALID = 2
        const val COMPRESS_EVERYTHING = 3
        const val COMPRESS_INVALID = 4
        const val SHOW_ALL = 5

        val defaultActionList = listOf(
            CANCEL,
            SKIP_INVALID,
            COMPRESS_EVERYTHING,
            COMPRESS_INVALID,
            SHOW_ALL,
        )
        val Int.toName: String
            get() = when (this) {
                NORMAL -> "NORMAL"
                CANCEL -> "CANCEL"
                SKIP_INVALID -> "SKIP_INVALID"
                COMPRESS_EVERYTHING -> "COMPRESS_EVERYTHING"
                COMPRESS_INVALID -> "COMPRESS_INVALID"
                else -> "ERROR"
            }

        fun String.stripPrefix() = this.substring(startIndex = prefixLength)
        fun String.getPrefix() = this.substring(startIndex = 0, endIndex = prefixLength)
    }
}
