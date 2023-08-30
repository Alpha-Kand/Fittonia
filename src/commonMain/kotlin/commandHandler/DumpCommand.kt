package commandHandler

import requireNull

object DumpCommand : Command() {
    private var path: String? = null

    fun getDumpPath() = verifyArgumentIsSet(argument = path, reportingName = pathArguments.first())

    override fun verify() {
        getDumpPath()
    }

    override fun addArg(argumentName: String, value: String) {
        try {
            if (pathArguments.contains(argumentName)) {
                requireNull(path)
                path = value
                return
            }
            throw IllegalArgumentException("This command does not take this argument: $argumentName")
        } catch (e: IllegalStateException) {
            throw IllegalStateException("Duplicate argument found: $argumentName")
        }
    }
}
