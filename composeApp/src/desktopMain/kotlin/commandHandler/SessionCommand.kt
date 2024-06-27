package commandHandler

data object SessionCommand : Command {
    override fun verify() {}
    override suspend fun addArg(
        argumentName: String,
        value: String,
    ) = tryCatch(argumentName = argumentName, value = value) {
        return@tryCatch false
    }
}
