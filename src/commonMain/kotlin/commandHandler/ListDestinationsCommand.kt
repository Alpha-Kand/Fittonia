package commandHandler

import requireNull

class ListDestinationsCommand : Command {
    private var name: String? = null
    private var search: String? = null

    fun getName() = name
    fun getSearch() = name

    override fun verify() {}

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        if (nameArguments.contains(argumentName)) {
            requireNull(name)
            name = value
            return@tryCatch true
        }
        if (searchArguments.contains(argumentName)) {
            requireNull(search)
            search = value
            return@tryCatch true
        }
        return@tryCatch false
    }
}
