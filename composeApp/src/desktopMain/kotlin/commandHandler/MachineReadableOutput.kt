package commandHandler

class MachineReadableOutput {
    var ioFormat: Boolean = false
    fun handleMachineReadableOutputFlag(argumentName: String): Boolean {
        return if (machineReadableOutputArguments.contains(argumentName)) {
            ioFormat = true
            true
        } else {
            false
        }
    }
}
