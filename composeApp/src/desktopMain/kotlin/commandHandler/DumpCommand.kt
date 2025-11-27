package commandHandler

import requireNull

class DumpCommand : Command(), HelpDocs {
    override var hasHelped = defaultHelped()
    private var path: String? = null

    fun getDumpPath() = path

    override fun verify() {}

    override suspend fun addArg(
        argumentName: String,
        value: String,
    ) = tryCatch(argumentName = argumentName, value = value) {
        if (showHelp(argumentName, dumpCommand)) return@tryCatch true
        if (pathArguments.contains(argumentName)) {
            requireNull(path)
            path = value
            return@tryCatch true
        }
        return@tryCatch false
    }
}
