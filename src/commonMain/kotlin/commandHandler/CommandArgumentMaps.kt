package commandHandler

const val addCommand = "add"
const val removeCommand = "remove"
const val listDestinationsCommand = "list"
const val dumpCommand = "dump"
const val serverCommand = "server"
const val sendFilesCommand = "send"

val nameArguments = listOf("name", "-n")
val ipArguments = listOf("ip", "-i")
val passwordArguments = listOf("password", "-P", "pw")
val pathArguments = listOf("path", "dump", "-D")
val portArguments = listOf("port", "-p")
val destinationArguments = listOf("destination", "-d", "dest")
val filesArguments = listOf("files", "-f", "source", "-s")

class ServerFlags {
    companion object {
        const val CONFIRM = 50
        const val DENY = 51

        const val SEND_FILES = 100
        const val SEND_STRING = 101
    }
}
