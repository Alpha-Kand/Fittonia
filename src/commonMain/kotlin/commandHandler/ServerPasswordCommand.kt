package commandHandler

import commandHandler.Command.Companion.verifyArgumentIsSet
import requireNull

class ServerPasswordCommand : Command {
    private var newPassword: String? = null
    private var oldPassword: String? = null

    fun getNewPassword() = verifyArgumentIsSet(argument = newPassword, reportingName = newArguments.first())
    fun getOldPassword() = oldPassword

    override fun verify() {
        getNewPassword()
    }

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (newArguments.contains(argumentName)) {
            requireNull(newPassword)
            newPassword = value
            return@tryCatch true
        }
        if (oldArguments.contains(argumentName)) {
            requireNull(oldPassword)
            oldPassword = value
            return@tryCatch true
        }
        return@tryCatch false
    }
}
