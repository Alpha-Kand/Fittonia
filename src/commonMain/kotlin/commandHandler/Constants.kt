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
const val exitCommand = "exit"
const val sessionCommand = "session"

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
    exitCommand,
    sessionCommand,
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
val sessionArguments = listOf("--session", "-s")

class ServerFlags {
    companion object {
        const val CONFIRM = 50
        const val DENY = 51

        const val SEND_FILES = 100
        const val SEND_MESSAGE = 101
        const val ADD_DESTINATION = 102

        const val HAVE_JOB_NAME = 103
        const val NEED_JOB_NAME = 104

        const val DONE = 105
        const val HAS_MORE = 106
    }
}

class FileTransfer {
    companion object {
        const val tempPrefix = "fittonia"
        const val tempSuffix = ".fittonia"

        const val filePrefix = "F?"
        const val dirPrefix = "D?"
    }
}
