package commandHandler

class IPCodeCommand : Command() {
    override fun verify() {}
    override suspend fun addArg(
        argumentName: String,
        value: String,
    ) = tryCatch(argumentName = argumentName, value = value) {
        return@tryCatch machineReadableOutput.handleMachineReadableOutputFlag(argumentName = argumentName)
    }
}
