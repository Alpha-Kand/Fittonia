package commandHandler

import requireNull

object DumpCommand : Command() {
    private var path: String? = null

    fun getDumpPath() = path

    override fun verify() {}

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
