package commandHandler

import commandHandler.Command.Companion.verifyArgumentIsSet
import requireNull

class DecodeIPCodeCommand : Command, MachineReadableOutput {
    override var ioFormat: Boolean = machineReadableDefault()
    private var code: String? = null

    fun getCode() = verifyArgumentIsSet(argument = code, reportingName = destinationArguments.first())

    override fun verify() {}
    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (handleMachineReadableOutputFlag(argumentName = argumentName)) {
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
