package commandHandler

import requireNull

class DecodeIPCodeCommand : Command() {
    private var code: String? = null

    fun getCode() = verifyArgumentIsSet(argument = code, reportingName = destinationArguments.first())

    override fun verify() {}
    override suspend fun addArg(
        argumentName: String,
        value: String,
    ) = tryCatch(argumentName = argumentName, value = value) {
        if (machineReadableOutput.handleMachineReadableOutputFlag(argumentName = argumentName)) {
            return@tryCatch true
        }
        if (ipCodeArguments.contains(argumentName)) {
            requireNull(code)
            code = value
            return@tryCatch true
        }
        return@tryCatch false
    }
}
