package commandHandler

object ListDestinationsCommand : Command() {

    override fun verify() {}
    override fun addArg(argumentName: String, value: String) {
        throw IllegalArgumentException("This command does not take any arguments.")
    }
}