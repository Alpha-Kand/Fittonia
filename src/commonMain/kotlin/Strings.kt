// TODO: Use up to date kotlin multiplatform methods.

// COMMANDS
const val noValidCommand = "No valid command detected."

// DecodeIpCode
const val blankIpCode = "No IP code provided."
const val couldNotDecodeIp = "Could not decode the IP code."

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

    fun search(term: String) = listOf(title, description, format).any { it.contains(term, ignoreCase = true) }
}

data object EncodeIpHelpDoc : HelpDoc {
    override val title = "ip-code"
    override val description =
        "Displays the current devices IPv4 address and its IP 'code'. This code is a human readable format intended to be shared and inputted on other devices instead of having to remember an entire IP address."
    override val format = "ip-code"
}

data object DecodeIPCodeHelpDoc : HelpDoc {
    override val title = "decode-ip-code"
    override val description = "Displays the IPv4 address associated with the provided IP 'code'."
    override val format = "decode-ip-code <CODE>"
}

data object ExitHelpDoc : HelpDoc {
    override val title = "exit OR quit"
    override val description = "Closes the program."
    override val format = "exit"
}
