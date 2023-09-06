package commandHandler

import requireNull

object AddCommand : Command() {
    private var name: String? = null
    private var ip: String? = null
    private var password: String? = null
    private var port: Int? = null

    fun getName() = verifyArgumentIsSet(argument = name, reportingName = nameArguments.first())
    fun getIP() = verifyArgumentIsSet(argument = ip, reportingName = ipArguments.first())
    fun getPassword() = verifyArgumentIsSet(argument = password, reportingName = passwordArguments.first())
    fun getPort() = port

    override fun verify() {
        getName()
        getIP()
        getPassword()
    }

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (nameArguments.contains(argumentName)) {
            requireNull(name)
            name = value
            return@tryCatch true
        }
        if (ipArguments.contains(argumentName)) {
            requireNull(ip)
            ip = value
            return@tryCatch true
        }
        if (passwordArguments.contains(argumentName)) {
            requireNull(password)
            password = value
            return@tryCatch true
        }
        if (portArguments.contains(argumentName)) {
            requireNull(port)
            port = value.toInt()
            return@tryCatch true
        }
        return@tryCatch false
    }
}
