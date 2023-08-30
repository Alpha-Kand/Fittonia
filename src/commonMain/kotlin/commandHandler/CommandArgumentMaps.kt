package commandHandler

const val addCommand = "add"
const val removeCommand = "remove"
const val listDestinationsCommand = "list"
const val dumpCommand = "dump"

val nameArguments = listOf("name", "-n")
val ipArguments = listOf("ip", "-i")
val passwordArguments = listOf("password", "-p", "pw")
val pathArguments = listOf("path", "dump", "-d")