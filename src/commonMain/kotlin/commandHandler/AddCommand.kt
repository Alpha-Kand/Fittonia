package commandHandler

import commandHandler.Command.Companion.verifyArgumentIsSet
import requireNull

class AddCommand : SendCommand(), MachineReadableOutput {
    override var ioFormat: Boolean = machineReadableDefault()
    private var name: String? = null

    fun getName() = verifyArgumentIsSet(argument = name, reportingName = nameArguments.first())
    override fun getDestination() = null

    override fun verify() {
        super.verify()
        getName()
    }

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (handleMachineReadableOutputFlag(argumentName = argumentName)) {
            return@tryCatch true
        }
        if (handleSendCommandArgument(argumentName = argumentName, value = value)) {
            return@tryCatch true
        }
        if (nameArguments.contains(argumentName)) {
            requireNull(name)
            name = value
            return@tryCatch true
        }
        return@tryCatch false
    }
}
