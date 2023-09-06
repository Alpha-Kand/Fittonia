package commandHandler

import requireNull

object SendFilesCommand : Command() {
    private var port: String? = null
    private var destination: String? = null
    private var files: String? = null
    private var ip: String? = null
    private var password: String? = null

    fun getPort() = verifyArgumentIsSet(argument = port, reportingName = portArguments.first()).toInt()
    fun getDestination() = destination
    fun getFiles() = verifyArgumentIsSet(argument = files, reportingName = filesArguments.first())
    fun getIP() = verifyArgumentIsSet(argument = ip, reportingName = ipArguments.first())
    fun getPassword() = verifyArgumentIsSet(argument = password, reportingName = passwordArguments.first())

    override fun verify() {
        if (destination == null) {
            getIP()
            getPassword()
        }
        getPort()
        getFiles()
    }

    override fun addArg(argumentName: String, value: String) {
        try {
            if (portArguments.contains(argumentName)) {
                requireNull(port)
                port = value
                return
            }
            if (destinationArguments.contains(argumentName)) {
                requireNull(destination)
                destination = value
                return
            }
            if (filesArguments.contains(argumentName)) {
                requireNull(files)
                files = value
                return
            }
            throw IllegalArgumentException("This command does not take this argument: $argumentName")
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Duplicate argument found: $argumentName")
        }
    }
}