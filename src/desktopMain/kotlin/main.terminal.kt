import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.runtime.Session
import commandHandler.AddCommand
import commandHandler.CommandHandler
import commandHandler.DumpCommand
import commandHandler.ExitCommand
import commandHandler.ListDestinationsCommand
import commandHandler.RemoveCommand
import commandHandler.SendFilesCommand
import commandHandler.SendMessageCommand
import commandHandler.ServerCommand
import commandHandler.ServerPasswordCommand
import commandHandler.SessionCommand
import commandHandler.SetDefaultPortCommand
import commandHandler.executeCommand.addExecution
import commandHandler.executeCommand.dumpExecution
import commandHandler.executeCommand.listDestinationsExecution
import commandHandler.executeCommand.removeExecution
import commandHandler.executeCommand.sendExecution.sendCommandExecution
import commandHandler.executeCommand.sendExecution.startClientEngine
import commandHandler.executeCommand.serverExecution
import commandHandler.executeCommand.serverPasswordExecution
import commandHandler.executeCommand.setDefaultPortExecution
import hmeadowSocket.HMeadowSocket
import settingsManager.SettingsManager

fun main(args: Array<String>) = session {
    KotterSession.kotter = this
    try {
        val settings = SettingsManager.settingsManager
        settings.registerAsMainProcess()
        settings.saveSettings()
        if (args.isNotEmpty()) {
            handleArguments(args.toList())
        } else {
            // TODO show version & --version & --help.
        }

        while (SessionManager.sessionActive) {
            var commandLine = ""
            section {
                text("> "); input()
            }.runUntilInputEntered {
                onInputEntered { commandLine = input }
            }
            handleArguments(inputTokens = SessionManager.getSessionParams(commandLine.split(" ")))
        }
    } catch (e: FittoniaError) {
        reportFittoniaError(e, prefix = "Terminal")
    } catch (e: HMeadowSocket.HMeadowSocketError) {
        reportHMSocketError(e)
    }
}

fun Session.handleArguments(inputTokens: List<String>) {
    when (val command = CommandHandler(args = inputTokens).getCommand()) {
        is AddCommand -> addExecution(command = command, inputTokens = inputTokens)
        is RemoveCommand -> removeExecution(command = command)
        is ListDestinationsCommand -> listDestinationsExecution(command = command)
        is DumpCommand -> dumpExecution(command = command)
        is ServerCommand -> serverExecution(command = command)
        is SendFilesCommand -> sendCommandExecution(command = command, inputTokens = inputTokens)
        is SendMessageCommand -> startClientEngine(inputTokens = inputTokens)
        is SetDefaultPortCommand -> setDefaultPortExecution(command = command)
        is ServerPasswordCommand -> serverPasswordExecution(command = command)
        is ExitCommand -> SessionManager.sessionActive = false
        is SessionCommand -> return
        else -> throw IllegalStateException("No valid command detected.")
    }
}
