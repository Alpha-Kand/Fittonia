import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.input.setInput
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.rgb
import com.varabyte.kotter.foundation.text.text
import commandHandler.AddCommand
import commandHandler.CommandHandler
import commandHandler.DecodeIPCodeCommand
import commandHandler.DumpCommand
import commandHandler.ExitCommand
import commandHandler.HelpCommand
import commandHandler.IPCodeCommand
import commandHandler.ListDestinationsCommand
import commandHandler.LogsCommand
import commandHandler.SendFilesCommand
import commandHandler.SendMessageCommand
import commandHandler.ServerCommand
import commandHandler.ServerPasswordCommand
import commandHandler.SetDefaultPortCommand
import commandHandler.executeCommand.addExecution
import commandHandler.executeCommand.decodeIpCodeExecution
import commandHandler.executeCommand.dumpExecution
import commandHandler.executeCommand.encodeIpCodeExecution
import commandHandler.executeCommand.helpExecution
import commandHandler.executeCommand.listDestinationsExecution
import commandHandler.executeCommand.logsExecution
import commandHandler.executeCommand.sendExecution.sendFilesExecution
import commandHandler.executeCommand.sendExecution.sendMessageExecution
import commandHandler.executeCommand.serverExecution
import commandHandler.executeCommand.serverPasswordExecution
import commandHandler.executeCommand.setDefaultPortExecution
import settingsManager.SettingsManager

fun main(args: Array<String>) = session {
    KotterSession.kotter = this
    var activeSession = true
    val settings = SettingsManager.settingsManager
    settings.registerAsMainProcess()
    settings.saveSettings()
    val previousInput = settings.previousCmdEntries
    while (activeSession) {
        var commandLine = ""
        section {
            rgb(value = 0x9EFFAB) {
                val serverStatus = when (LocalServer.isActive()) {
                    false -> ""
                    true -> "⏳ "
                }
                text("$serverStatus> "); input()
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
                is AddCommand -> addExecution(command = command)
                is DecodeIPCodeCommand -> decodeIpCodeExecution(command = command)
                is DumpCommand -> dumpExecution(command = command)
                is ExitCommand -> {
                    activeSession = false
                    settings.saveSettings()
                }

                is HelpCommand -> helpExecution(command = command)
                is IPCodeCommand -> encodeIpCodeExecution(command = command)
                is ListDestinationsCommand -> listDestinationsExecution(command = command)
                is LogsCommand -> logsExecution()
                is SendFilesCommand -> sendFilesExecution(command = command)
                is SendMessageCommand -> sendMessageExecution(command = command)
                is ServerCommand -> serverExecution(command = command)
                is ServerPasswordCommand -> serverPasswordExecution(command = command)
                is SetDefaultPortCommand -> setDefaultPortExecution(command = command)
                else -> printLine(text = noValidCommand)
            }
        } catch (e: FittoniaError) {
            reportFittoniaError2(e)
        }
    }
}
