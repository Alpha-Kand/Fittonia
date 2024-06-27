import commandHandler.Command.Companion.verifyArgumentIsSet
import commandHandler.SendCommand
import commandHandler.destinationArguments
import commandHandler.ipArguments
import commandHandler.passwordArguments
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
        var password: String? = null
            private set

        fun setSessionParams(command: SendCommand) {
            command.setFromSession()
            port = command.getPort()
            destination = command.getDestination()
            if (destination == null) {
                ip = command.getIP()
                password = command.getPassword()
            }
        }

        fun getSessionParams(input: List<String>): List<String> {
            if (input.first() != sendFilesCommand && input.first() != sendMessageCommand) {
                return input
            }
            var setPort = true
            var setDestination = true
            var setIP = true
            var setPassword = true

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
                passwordArguments.forEach {
                    if (token.startsWith(it)) {
                        setPassword = false
                    }
                }
            }
            // /TODODODODODODODODO TODO don't require ip and password if destination is already given.
            return listOfNotNull(
                input.first(),
                // Insert after command name, but before trailing 'send items'.
                portArguments.first().let {
                    "$it=${verifyArgumentIsSet(port, it)}"
                }.takeIf { setPort },
                destinationArguments.first().let {
                    "$it=${verifyArgumentIsSet(destination, it)}"
                }.takeIf { setDestination },
                passwordArguments.first().let {
                    "$it=${verifyArgumentIsSet(password, it)}"
                }.takeIf { setPassword },
                ipArguments.first().let {
                    "$it=${verifyArgumentIsSet(ip, it)}"
                }.takeIf { setIP },
            ) + input.subList(1, input.size)
        }
    }
}
