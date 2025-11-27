package commandHandler.executeCommand

import HelpDoc
import HelpDocLoader
import KotterSession.kotter
import OutputIO.printlnIO
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.textLine
import commandHandler.HelpCommand
import commandHandler.commands
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.format_label
import fittonia.composeapp.generated.resources.help_intro
import fittonia.composeapp.generated.resources.no_commands_found
import fittonia.composeapp.generated.resources.search_failed
import fittonia.composeapp.generated.resources.search_header
import forEachSuspended
import org.jetbrains.compose.resources.getString

suspend fun helpExecution(command: HelpCommand) {
    command.getCommandSearch()?.let { search ->
        printlnIO(Res.string.search_header, search)
        commands.mapNotNull { it.takeIf { it.contains(search) } }.let {
            if (it.isEmpty()) {
                printlnIO(Res.string.no_commands_found)
            } else {
                it.forEach(::printlnIO)
            }
        }
    } ?: command.getSearch()?.let { search ->
        HelpDocLoader.helpDocs.filter {
            it.search(term = search)
        }.also {
            if (it.isEmpty()) {
                printlnIO(Res.string.search_failed)
                return
            }
        }.forEachSuspended(::printHelpSection)
    } ?: run {
        printlnIO(Res.string.help_intro)
        HelpDocLoader.helpDocs.forEachSuspended(::printHelpSection)
    }
}

private suspend fun printHelpSection(helpDoc: HelpDoc) {
    val format = getString(Res.string.format_label, helpDoc.format)
    kotter.section {
        cyan { textLine(text = helpDoc.title) }
        textLine(text = helpDoc.description)
        textLine(text = format) // TODO - After release
        helpDoc.arguments?.forEach {
            textLine("${it.first.joinToString(separator = ", ")}: ${it.second}")
        }
        textLine()
    }.run()
}
