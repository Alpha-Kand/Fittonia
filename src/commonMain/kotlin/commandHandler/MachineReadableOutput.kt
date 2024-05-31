package commandHandler

interface MachineReadableOutput {
    var ioFormat: Boolean

    fun machineReadableDefault() = false

    fun handleMachineReadableOutputFlag(argumentName: String) =
        if (machineReadableOutputArguments.contains(argumentName)) {
            ioFormat = true
            true
        } else {
            false
        }
}
