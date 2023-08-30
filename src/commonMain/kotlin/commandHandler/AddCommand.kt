package commandHandler

import requireNull

object AddCommand : Command() {
    private var name: String? = null
    private var ip: String? = null
    private var password: String? = null

    fun getName() = verifyArgumentIsSet(argument = name, reportingName = nameArguments.first())
    fun getIP() = verifyArgumentIsSet(argument = ip, reportingName = ipArguments.first())
    fun getPassword() = verifyArgumentIsSet(argument = password, reportingName = passwordArguments.first())

    override fun verify() {
        getName()
        getIP()
        getPassword()
    }

    override fun addArg(argumentName: String, value: String) {
        try {
            if (nameArguments.contains(argumentName)) {
                requireNull(name)
                name = value
                return
            }
            if (ipArguments.contains(argumentName)) {
                requireNull(ip)
                ip = value
                return
            }
            if (passwordArguments.contains(argumentName)) {
                requireNull(password)
                password = value
                return
            }

            throw IllegalArgumentException("This command does not take this argument: $argumentName")
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Duplicate argument found: $argumentName")
        }
    }
}
