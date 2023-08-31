package commandHandler

import requireNull

object ListDestinationsCommand : Command() {
    private var name: String? = null

    fun getName() = name

    override fun verify() {}

    override fun addArg(argumentName: String, value: String) {
        try {
            if (nameArguments.contains(argumentName)) {
                requireNull(name)
                name = value
                return
            }

            throw IllegalArgumentException("This command does not take this argument: $argumentName")
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Duplicate argument found: $argumentName")
        }
    }
}
