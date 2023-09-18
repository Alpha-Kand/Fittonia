package commandHandler

import requireNull

object DumpCommand : Command {
    private var path: String? = null

    fun getDumpPath() = path

    override fun verify() {}

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (pathArguments.contains(argumentName)) {
            requireNull(path)
            path = value
            return@tryCatch true
        }
        return@tryCatch false
    }
}
