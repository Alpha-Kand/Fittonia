import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.input.setInput
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.rgb
import com.varabyte.kotter.foundation.text.text
import commandHandler.CommandHandler
import commandHandler.DecodeIPCodeCommand
import commandHandler.DumpCommand
import commandHandler.ExitCommand
import commandHandler.HelpCommand
import commandHandler.IPCodeCommand
import commandHandler.SetDefaultPortCommand
import commandHandler.executeCommand.decodeIpCodeExecution
import commandHandler.executeCommand.dumpExecution
import commandHandler.executeCommand.encodeIpCodeExecution
import commandHandler.executeCommand.helpExecution
import commandHandler.executeCommand.listDestinationsExecution
import commandHandler.executeCommand.setDefaultPortExecution
import settingsManager.SettingsManager
import java.util.LinkedList

fun main(args: Array<String>) = session {
    KotterSession.kotter = this

    var activeSession = true
    val settings = SettingsManager.settingsManager
    settings.registerAsMainProcess()
    val previousInput = LinkedList<String>()
    while (activeSession) {
        var commandLine = ""
        section {
            rgb(value = 0x9EFFAB) {
                text("> "); input()
            }
        }.runUntilInputEntered {
            onKeyPressed {
                when (key) {
                    Keys.UP -> {
                        setInput(text = previousInput.last)
                        previousInput.addFirst(previousInput.last)
                        previousInput.removeLast()
                    }

                    Keys.DOWN -> {
                        setInput(text = previousInput.first)
                        previousInput.addLast(previousInput.first)
                        previousInput.removeFirst()
                    }
                }
            }

            onInputEntered {
                commandLine = input
                previousInput.add(commandLine)
            }
        }
        try {
            when (val command = CommandHandler(args = commandLine.split(" ")).getCommand()) {
                is DecodeIPCodeCommand -> decodeIpCodeExecution(command = command)
                is DumpCommand -> dumpExecution(command = command)
                is ExitCommand -> activeSession = false
                is HelpCommand -> helpExecution(command = command)
                is IPCodeCommand -> encodeIpCodeExecution(command = command)
                is ListDestinationsCommand -> listDestinationsExecution(command = command)
                is SetDefaultPortCommand -> setDefaultPortExecution(command = command)
                else -> printLine(text = noValidCommand)
            }
        } catch (e: FittoniaError) {
            reportFittoniaError2(e)
        }
    }
}
