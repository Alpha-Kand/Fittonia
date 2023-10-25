package commandHandler

object SessionCommand : Command {
    override fun verify() {}
    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        return@tryCatch false
    }
}
