package commandHandler.executeCommand

import DecodeIPCodeHelpDoc
import DumpHelpDoc
import EncodeIpHelpDoc
import ExitHelpDoc
import HelpDoc
import KotterSession.kotter
import OutputIO.printlnIO
import SetDefaultPortHelpDoc
import com.varabyte.kotter.foundation.text.cyan
import com.varabyte.kotter.foundation.text.textLine
import commandHandler.HelpCommand
import commandHandler.commands
import formatLabel
import helpIntro
import noCommandsFound
import searchFailed
import searchHeader

fun helpExecution(command: HelpCommand) {
    command.getCommandSearch()?.let { search ->
        printlnIO(output = searchHeader.format(search))
        commands.mapNotNull { it.takeIf { it.contains(search) } }.let {
            if (it.isEmpty()) {
                printlnIO(output = noCommandsFound)
            } else {
                it.forEach(::printlnIO)
            }
        }
    } ?: command.getSearch()?.let { search ->
        helpDocs.filter {
            it.search(term = search)
        }.also {
            if (it.isEmpty()) {
                printlnIO(output = searchFailed)
                return
            }
        }.forEach(::printHelpSection)
    } ?: run {
        printlnIO(output = helpIntro)
        helpDocs.forEach(::printHelpSection)
    }
}

private val helpDocs = listOf(
    EncodeIpHelpDoc,
    DecodeIPCodeHelpDoc,
    DumpHelpDoc,
    SetDefaultPortHelpDoc,
    ExitHelpDoc,
)

private fun printHelpSection(helpDoc: HelpDoc) {
    kotter.section {
        cyan { textLine(text = helpDoc.title) }
        textLine(text = helpDoc.description)
        textLine(text = formatLabel.format(helpDoc.format))
        helpDoc.arguments?.forEach {
            textLine("${it.first.joinToString(separator = ", ")}: ${it.second}")
        }
        textLine()
    }.run()
}
