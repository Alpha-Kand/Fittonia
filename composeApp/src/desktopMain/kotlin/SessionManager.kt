import commandHandler.Command.Companion.verifyArgumentIsSet
import commandHandler.SendCommand
import commandHandler.accessCodeArguments
import commandHandler.destinationArguments
import commandHandler.ipArguments
import commandHandler.portArguments
import commandHandler.sendFilesCommand
import commandHandler.sendMessageCommand

class SessionManager {
    companion object {
        var sessionActive: Boolean = false
        var port: Int? = null
            private set
        var destination: String? = null
            private set
        var ip: String? = null
            private set
        var accessCode: String? = null
            private set

        fun setSessionParams(command: SendCommand) {
            command.setFromSession()
            port = command.getPort()
            destination = command.getDestination()
            if (destination == null) {
                ip = command.getIP()
                accessCode = command.getAccessCode()
            }
        }

        fun getSessionParams(input: List<String>): List<String> {
            if (input.first() != sendFilesCommand && input.first() != sendMessageCommand) {
                return input
            }
            var setPort = true
            var setDestination = true
            var setIP = true
            var setAccessCode = true

            input.forEach { token ->
                portArguments.forEach {
                    if (token.startsWith(it)) {
                        setPort = false
                    }
                }
                destinationArguments.forEach {
                    if (token.startsWith(it)) {
                        setDestination = false
                    }
                }
                ipArguments.forEach {
                    if (token.startsWith(it)) {
                        setIP = false
                    }
                }
                accessCodeArguments.forEach {
                    if (token.startsWith(it)) {
                        setAccessCode = false
                    }
                }
            }
            // TODO don't require ip and access code if destination is already given. - After release
            return listOfNotNull(
                input.first(),
                // Insert after command name, but before trailing 'send items'.
                portArguments.first().let {
                    "$it=${verifyArgumentIsSet(port, it)}"
                }.takeIf { setPort },
                destinationArguments.first().let {
                    "$it=${verifyArgumentIsSet(destination, it)}"
                }.takeIf { setDestination },
                accessCodeArguments.first().let {
                    "$it=${verifyArgumentIsSet(accessCode, it)}"
                }.takeIf { setAccessCode },
                ipArguments.first().let {
                    "$it=${verifyArgumentIsSet(ip, it)}"
                }.takeIf { setIP },
            ) + input.subList(1, input.size)
        }
    }
}
