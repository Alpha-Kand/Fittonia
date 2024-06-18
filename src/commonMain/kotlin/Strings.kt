import commandHandler.decodeIpCodeCommand
import commandHandler.dumpCommand
import commandHandler.exitCommand
import commandHandler.ipCodeArguments
import commandHandler.ipCodeCommand
import commandHandler.listDestinationsCommand
import commandHandler.nameArguments
import commandHandler.newArguments
import commandHandler.oldArguments
import commandHandler.pathArguments
import commandHandler.portArguments
import commandHandler.quitCommand
import commandHandler.serverPasswordCommand
import commandHandler.setDefaultPortCommand

// TODO: Use up to date kotlin multiplatform methods.

// FittoniaErrors
const val errorAddDestinationAlreadyExists =
    "A destination with that name is already registered. Delete the old one and try again."

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

// List
const val nameOutput = "Name: %1\$s"
const val ipOutput = "IP: %1\$s"
const val destinationNotFound = "No destination found with that name."
const val noDestinationsRegistered = "No destinations registered. Register new ones with the 'add' command."

// ServerPassword
const val noPreviousPasswordFound = "No previous password found. New password not saved."
const val confirmPasswordUpdate = "Please provide current (old) password to confirm changes."
const val confirmPasswordUpdateFailed = "Old password was incorrect, please try again."
const val passwordSaved =
    "Password saved. You will need to provide this new password if you want to change the default password again."

// Server
const val serverInitCannotStartNoPassword =
    "Cannot start server: No server password set. Set it with the `server-password` command."
const val serverInitCannotStartNoDumpPath =
    "Cannot start server: No dump path set (directory path to store files from clients). Set it with the `dump` command."
const val serverInitServerStarted = "Server started. ⏳ Waiting for clients."
const val serverInitServerAlreadyStarted = "Server already started."

// SendFiles
const val cannotSendFilePathTooLong =
    "The destination cannot receive %1\$s file(s) because their total paths would be too long (> 127 characters):"
const val sendFilesUserOptionCancel = "%1\$s. Cancel sending files."
const val sendFilesUserOptionSkipInvalid = "%1\$s. Skip invalid files."
const val sendFilesUserOptionCompressAll = "%1\$s. Compress all files and send as a single file."
const val sendFilesUserOptionCompressInvalid =
    "%1\$s. Compress invalid files only and send as a single file (relative file paths will be preserved)."
const val sendFilesUserOptionShowAll = "%1\$s. Show all files and ask again."

// HelpCommand
const val searchHeader = "Commands containing \"%1\$s\":"
const val noCommandsFound = "No commands found."
const val searchFailed = "Search term not found."
const val formatLabel = "Format: %1\$s"
const val helpIntro = """Fittonia v1.0
By Hunter Wiesman 'huntersmeadow@gmail.com'

COMMANDS:
"""

// SetDefaultPortCommand
const val currentDefaultPortIsSetTo = "Current default port set to: %1\$s"

interface HelpDoc {
    val title: String
    val description: String
    val format: String
    val arguments: List<Pair<List<String>, String>>?

    fun search(term: String) = listOf(title, description, format).any { it.contains(term, ignoreCase = true) }
}

data object DecodeIPCodeHelpDoc : HelpDoc {
    override val title = decodeIpCodeCommand
    override val description = "Displays the IPv4 address associated with the provided IP 'code'."
    override val format = "$decodeIpCodeCommand <CODE>"
    override val arguments = listOf(
        ipCodeArguments to "Code in the format of WORD-WORD-NUMBER. e.g. cat-dog-12",
    )
}

data object DumpHelpDoc : HelpDoc {
    override val title = dumpCommand
    override val description =
        "Updates or prints the current 'dump' path. The dump path is where all the incoming files are put categorized by 'job'. e.g. /.../dumpPath/jobName/file.txt.\nRun without input to print the current dump path."
    override val format = "$dumpCommand <OPTIONAL PATH>"
    override val arguments = listOf(
        pathArguments to "Path to put incoming files and folders.",
    )
}

data object EncodeIpHelpDoc : HelpDoc {
    override val title = ipCodeCommand
    override val description =
        "Displays the current devices IPv4 address and its IP 'code'. This code is a human readable format intended to be shared and inputted on other devices instead of having to remember an entire IP address."
    override val format = ipCodeCommand
    override val arguments = null
}

data object ExitHelpDoc : HelpDoc {
    override val title = "$exitCommand OR $quitCommand"
    override val description = "Closes the program."
    override val format = exitCommand
    override val arguments = null
}

data object ListHelpDoc : HelpDoc {
    override val title = listDestinationsCommand
    override val description =
        "Prints out the list of registered destinations. Enter a name to search the list instead."
    override val format = "$listDestinationsCommand <OPTIONAL NAME>"
    override val arguments = listOf(
        nameArguments to "Name of a specific destination to find.",
    )
}

data object SetDefaultPortHelpDoc : HelpDoc {
    override val title = setDefaultPortCommand
    override val description = "Sets the default port to use for incoming connections."
    override val format = "$setDefaultPortCommand <PORT>"
    override val arguments = listOf(
        portArguments to "Standardized port number. e.g. 8080",
    )
}

data object ServerPasswordHelpDoc : HelpDoc {
    override val title = serverPasswordCommand
    override val description = "Sets the server's password that incoming connections must provide to connect."
    override val format = "$serverPasswordCommand <NEW PASSWORD> <OPTIONAL OLD PASSWORD>"
    override val arguments = listOf(
        newArguments to "The new server password.",
        oldArguments to "Old password, if previously set.",
    )
}
