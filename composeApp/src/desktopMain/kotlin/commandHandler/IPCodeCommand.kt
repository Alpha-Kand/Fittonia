package commandHandler

class IPCodeCommand : Command, MachineReadableOutput {
    override var ioFormat: Boolean = machineReadableDefault()
    override fun verify() {}
    override suspend fun addArg(
        argumentName: String,
        value: String,
    ) = tryCatch(argumentName = argumentName, value = value) {
        return@tryCatch handleMachineReadableOutputFlag(argumentName = argumentName)
    }
}
