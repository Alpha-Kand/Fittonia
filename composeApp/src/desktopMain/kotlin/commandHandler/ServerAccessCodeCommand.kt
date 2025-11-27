package commandHandler

import requireNull

class ServerAccessCodeCommand : Command() {
    private var newAccessCode: String? = null
    private var oldAccessCode: String? = null

    fun getNewAccessCode() = verifyArgumentIsSet(argument = newAccessCode, reportingName = newArguments.first())
    fun getOldAccessCode() = oldAccessCode

    override fun verify() {
        getNewAccessCode()
    }

    override suspend fun addArg(
        argumentName: String,
        value: String,
    ) = tryCatch(argumentName = argumentName, value = value) {
        if (newArguments.contains(argumentName)) {
            requireNull(newAccessCode)
            newAccessCode = value
            return@tryCatch true
        }
        if (oldArguments.contains(argumentName)) {
            requireNull(oldAccessCode)
            oldAccessCode = value
            return@tryCatch true
        }
        return@tryCatch false
    }
}
