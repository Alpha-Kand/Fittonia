import commandHandler.ipCodeArguments
import commandHandler.pathArguments

// TODO: Use up to date kotlin multiplatform methods.

// COMMANDS
const val noValidCommand = "No valid command detected."

// DecodeIpCode
const val blankIpCode = "No IP code provided."
const val couldNotDecodeIp = "Could not decode the IP code."

// Dump
const val dumpPathNotEmptyWarning = "New dump path is not empty."
const val dumpPathNotWritable = "That directory is not writable. Check its permissions."
const val dumpPathNotValidDirectory = "Supplied path was not a valid directory."
const val dumpPathDoesntExist = "Supplied path does not exist."
const val dumpPathNotSet = "No dump path set."
const val dumpPathCurrent = "Current dump path: %1\$s"

// EncodeIpCode
const val yourIpAddress = "Your IP address = %1\$s"
const val ipAddressCode = "IP address code = %1\$s"
const val couldNotEncodeIp = "Could not encode IP address."

// HelpCommand
const val searchHeader = "Commands containing \"%1\$s\":"
const val noCommandsFound = "No commands found."
const val searchFailed = "Search term not found."
const val formatLabel = "Format: %1\$s"
const val helpIntro = """Fittonia v1.0
Made by Hunter Wiesman 'huntersmeadow@gmail.com'

COMMANDS:
"""

interface HelpDoc {
    val title: String
    val description: String
    val format: String
    val arguments: List<Pair<List<String>, String>>?

    fun search(term: String) = listOf(title, description, format).any { it.contains(term, ignoreCase = true) }
}

data object DecodeIPCodeHelpDoc : HelpDoc {
    override val title = "decode-ip-code"
    override val description = "Displays the IPv4 address associated with the provided IP 'code'."
    override val format = "decode-ip-code <CODE>"
    override val arguments = listOf(
        ipCodeArguments to "Code in the format of WORD-WORD-NUMBER. e.g. cat-dog-12",
    )
}

data object DumpHelpDoc : HelpDoc {
    override val title = "dump"
    override val description =
        "Updates or prints the current 'dump' path. The dump path is where all the incoming files are put categorized by 'job'. e.g. /.../dumpPath/jobName/file.txt.\nRun without input to print the current dump path."
    override val format = "dump <OPTIONAL PATH>"
    override val arguments = listOf(
        pathArguments to "Path to put incoming files and folders.",
    )
}

data object EncodeIpHelpDoc : HelpDoc {
    override val title = "ip-code"
    override val description =
        "Displays the current devices IPv4 address and its IP 'code'. This code is a human readable format intended to be shared and inputted on other devices instead of having to remember an entire IP address."
    override val format = "ip-code"
    override val arguments = null
}

data object ExitHelpDoc : HelpDoc {
    override val title = "exit OR quit"
    override val description = "Closes the program."
    override val format = "exit"
    override val arguments = null
}
