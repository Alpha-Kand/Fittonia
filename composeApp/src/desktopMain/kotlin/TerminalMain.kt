import KotterSession.kotter
import OutputIO.printlnIO
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.input.setInput
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.rgb
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import commandHandler.AddCommand
import commandHandler.CommandHandler
import commandHandler.DecodeIPCodeCommand
import commandHandler.DumpCommand
import commandHandler.ExitCommand
import commandHandler.HasHelpedException
import commandHandler.HelpCommand
import commandHandler.HelpDocs
import commandHandler.IPCodeCommand
import commandHandler.ListDestinationsCommand
import commandHandler.LogsCommand
import commandHandler.RemoveCommand
import commandHandler.SendFilesCommand
import commandHandler.SendMessageCommand
import commandHandler.ServerAccessCodeCommand
import commandHandler.ServerCommand
import commandHandler.SetDefaultPortCommand
import commandHandler.executeCommand.addExecution
import commandHandler.executeCommand.decodeIpCodeExecution
import commandHandler.executeCommand.dumpExecution
import commandHandler.executeCommand.encodeIpCodeExecution
import commandHandler.executeCommand.helpExecution
import commandHandler.executeCommand.listDestinationsExecution
import commandHandler.executeCommand.logsExecution
import commandHandler.executeCommand.removeExecution
import commandHandler.executeCommand.sendExecution.sendFilesExecution
import commandHandler.executeCommand.sendExecution.sendMessageExecution
import commandHandler.executeCommand.serverAccessCodeExecution
import commandHandler.executeCommand.serverExecution
import commandHandler.executeCommand.setDefaultPortExecution
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.no_valid_command
import kotlinx.coroutines.runBlocking

fun terminalMain() = session {
    kotter = this
    runBlocking {
        var activeSession = true
        val settings = SettingsManagerDesktop.settingsManager
        settings.saveSettings()
        HelpDocLoader.init()
        val previousInput = settings.previousCmdEntries
        while (activeSession) {
            var commandLine = ""
            section {
                rgb(value = 0x9EFFAB) {
                    val serverStatus = when (DesktopServer.isActive()) {
                        false -> ""
                        true -> "⏳ "
                    }
                    text("$serverStatus> ")
                    input()
                }
            }.runUntilInputEntered {
                onKeyPressed {
                    when (key) {
                        Keys.UP -> {
                            setInput(text = previousInput.last())
                            previousInput.addFirst(previousInput.last())
                            previousInput.removeLast()
                        }

                        Keys.DOWN -> {
                            setInput(text = previousInput.first())
                            previousInput.addLast(previousInput.first())
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
                val command = CommandHandler(args = commandLine.split(" ")).getCommand().also {
                    if (it is HelpDocs && it.hasHelped) {
                        throw HasHelpedException()
                    }
                }
                when (command) {
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
                    is RemoveCommand -> removeExecution(command = command)
                    is SendFilesCommand -> sendFilesExecution(command = command)
                    is SendMessageCommand -> sendMessageExecution(command = command)
                    is ServerCommand -> serverExecution(command = command)
                    is ServerAccessCodeCommand -> serverAccessCodeExecution(command = command)
                    is SetDefaultPortCommand -> setDefaultPortExecution(command = command)
                    else -> printlnIO(Res.string.no_valid_command)
                }
            } catch (e: HasHelpedException) {
                // Eat it.
            } catch (e: FittoniaError) {
                e.getErrorMessage().let {
                    section {
                        red { text("Error: ") }
                        textLine(it)
                    }.run()
                }
            }
        }
    }
}
