package commandHandler

import FittoniaError
import FittoniaErrorType
import OutputIO.printlnIO
import alsoIfTrue
import requireNull

class HelpCommand : Command {
    private var commandSearch: String? = null
    private var search: String? = null

    fun getCommandSearch() = commandSearch
    fun getSearch() = search

    override fun verify() {
        if (commandSearch != null && search != null) {
            printlnIO(commandSearch.toString())
            printlnIO(search.toString())
            throw FittoniaError(FittoniaErrorType.TOO_MANY_SEARCH_TERMS)
        }
    }

    override fun addArg(argumentName: String, value: String) = tryCatch(argumentName = argumentName, value = value) {
        return@tryCatch listOf(
            searchCommandsArguments.contains(argumentName).alsoIfTrue {
                requireNull(commandSearch)
                commandSearch = value
            },
            searchArguments.contains(argumentName).alsoIfTrue {
                requireNull(search)
                search = value
            },
        ).any { it }
    }
}
