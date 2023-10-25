import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.Color
import com.varabyte.kotter.foundation.text.color
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.text.white
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
import commandHandler.ServerFlags
import commandHandler.ServerPasswordCommand
import commandHandler.SessionCommand
import commandHandler.SetDefaultPortCommand
import commandHandler.executeCommand.addExecution
import commandHandler.executeCommand.dumpExecution
import commandHandler.executeCommand.listDestinationsExecution
import commandHandler.executeCommand.removeExecution
import commandHandler.executeCommand.serverExecution
import commandHandler.executeCommand.serverPasswordExecution
import commandHandler.executeCommand.setDefaultPortExecution
import hmeadowSocket.HMeadowSocket
import hmeadowSocket.HMeadowSocketServer
import settingsManager.SettingsManager
import java.io.File
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) = session {
    try {
        SettingsManager.settingsManager.saveSettings()
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
            handleArguments(input = SessionManager.getSessionParams(commandLine.split(" ")))
        }
    } catch (e: FittoniaError) {
        reportFittoniaError(e, prefix = "Terminal")
    } catch (e: HMeadowSocket.HMeadowSocketError) {
        reportHMSocketError(e)
    }
}

fun Session.handleArguments(input: List<String>) {
    when (val command = CommandHandler(args = input).getCommand()) {
        is AddCommand -> addExecution(command = command)
        is RemoveCommand -> removeExecution(command = command)
        is ListDestinationsCommand -> listDestinationsExecution(command = command)
        is DumpCommand -> dumpExecution(command = command)
        is ServerCommand -> serverExecution(command = command)
        is SendFilesCommand,
        is SendMessageCommand,
        -> {
            val clientEngineConsole = object : Thread() {
                override fun run() {
                    section {
                        val server = HMeadowSocketServer.getServer(port = 10778)
                        while (true) {
                            when (server.receiveInt()) {
                                ServerFlags.HAS_MORE -> {
                                    color(Color.values()[server.receiveInt()])
                                    when (server.receiveInt()) {
                                        1 -> textLine(text = server.receiveString())
                                        0 -> text(text = server.receiveString())
                                        else -> Unit // ???
                                    }
                                    white()
                                }

                                ServerFlags.DONE -> break
                            }
                        }
                    }.run()
                }
            }
            clientEngineConsole.start()
            val currentDirectory = System.getProperty("user.dir")
            val clientEngineCmdLine = StringBuilder()
                .append("java -jar $currentDirectory/build/compose/jars/FittoniaClientEngine-linux-x64-1.0.jar")
            input.forEach {
                clientEngineCmdLine.append(' ')
                clientEngineCmdLine.append(it)
            }

            ProcessBuilder(*clientEngineCmdLine.toString().split(' ').toTypedArray())
                .directory(File(currentDirectory))
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor(60, TimeUnit.MINUTES)
        }

        is SetDefaultPortCommand -> setDefaultPortExecution(command = command)
        is ServerPasswordCommand -> serverPasswordExecution(command = command)
        is ExitCommand -> SessionManager.sessionActive = false
        is SessionCommand -> return
        else -> throw IllegalStateException("No valid command detected.")
    }
}
