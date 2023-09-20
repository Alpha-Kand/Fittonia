package commandHandler

import requireNull

class SetDefaultPortCommand : Command {
    private var port: Int? = null
    private var clear: Boolean = false

    fun getPort() = port
    fun getClear() = clear

    override fun verify() {
        if (verifyPortNumber(port) && clear) {
            throw IllegalStateException("Cannot set and reset default port at the same time.")
        }
    }

    override fun addArg(argumentName: String, value: String) {
        try {
            if (portArguments.contains(argumentName)) {
                requireNull(port)
                port = value.toInt()
                return
            }

            if (clearArguments.contains(argumentName)) {
                if (value.isEmpty()) {
                    clear = true
                    return
                } else {
                    throw IllegalArgumentException("This argument does not take a value: $argumentName")
                }
            }
            throw IllegalArgumentException("This command does not take this argument: $argumentName")
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Duplicate argument found: $argumentName")
        } catch (e: NumberFormatException) {
            throw IllegalStateException("Non-numerical port: $value")
        }
    }
}
