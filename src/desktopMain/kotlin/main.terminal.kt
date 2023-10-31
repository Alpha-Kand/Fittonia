import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.Color
import com.varabyte.kotter.foundation.text.color
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import commandHandler.AddCommand
import commandHandler.CommandHandler
import commandHandler.DumpCommand
import commandHandler.ExitCommand
import commandHandler.FileTransfer
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
        is AddCommand -> addExecution(command = command)
        is RemoveCommand -> removeExecution(command = command)
        is ListDestinationsCommand -> listDestinationsExecution(command = command)
        is DumpCommand -> dumpExecution(command = command)
        is ServerCommand -> serverExecution(command = command)
        is SendFilesCommand,
        is SendMessageCommand,
        -> {
            val spawnClientEngine = object : Thread() {
                override fun run() {
                    val currentDirectory = System.getProperty("user.dir")
                    val clientEngineCmdLine = StringBuilder()
                        .append("java -jar $currentDirectory/build/compose/jars/FittoniaClientEngine-linux-x64-1.0.jar")
                    inputTokens.forEach {
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
            }
            spawnClientEngine.start()
            val child = HMeadowSocketServer.getServer(port = 10778)
            while (true) {
                when (child.receiveInt()) {
                    ServerFlags.PRINT_LINE -> {
                        section {
                            color(Color.values()[child.receiveInt()])
                            textLine(text = child.receiveString())
                        }.run()
                    }

                    ServerFlags.FILE_NAMES_TOO_LONG -> {
                        val serverDestinationDirLength = child.receiveInt()
                        val filePaths = mutableListOf<String>()
                        while (child.receiveInt() == ServerFlags.HAS_MORE) {
                            filePaths.add(child.receiveString())
                        }

                        val pathCount = filePaths.size

                        printLine(
                            text = "The destination cannot receive $pathCount file(s) because their total paths would be too long (> 127 characters):",
                            color = Color.YELLOW,
                        )
                        printLine()

                        val previewLimit = 10
                        val pathCountOverCutoff = pathCount > previewLimit

                        var userInput = 0
                        while (true) {
                            val previewCutoff = pathCountOverCutoff && userInput != FileTransfer.SHOW_ALL
                            if (previewCutoff) {
                                filePaths.subList(0, previewLimit)
                            } else {
                                filePaths
                            }.forEachIndexed { index, path ->
                                renderCutoffPath(
                                    path = path,
                                    serverDestinationDirLength = serverDestinationDirLength,
                                    index = index,
                                )
                            }

                            if (previewCutoff) {
                                printLine(text = "...")
                                renderCutoffPath(
                                    path = filePaths.last(),
                                    serverDestinationDirLength = serverDestinationDirLength,
                                    index = filePaths.lastIndex,
                                )
                            }

                            section {
                                textLine()
                                if (pathCountOverCutoff) {
                                    textLine(text = "What would you like to do? (1,2,3,4)")
                                } else {
                                    textLine(text = "What would you like to do? (1,2,3)")
                                }
                                FileTransfer.actionList.forEach { action ->
                                    when (action) {
                                        FileTransfer.CANCEL -> textLine(text = "$action. Cancel sending files.")
                                        FileTransfer.SKIP_INVALID -> textLine(text = "$action. Skip invalid files.")
                                        FileTransfer.COMPRESS_EVERYTHING -> textLine(text = "$action. Compress all files and send as a single file.")
                                        FileTransfer.COMPRESS_INVALID -> textLine(text = "$action. Compress invalid files only and send as a single file (relative file paths will be preserved).")
                                        FileTransfer.SHOW_ALL -> if (pathCountOverCutoff) {
                                            textLine(text = "$action. Show all files and ask again.")
                                        }
                                    }
                                }
                                text(text = "> "); input()
                            }.runUntilInputEntered {
                                onInputEntered {
                                    val availableActionRange = IntRange(
                                        start = 1,
                                        endInclusive = if (pathCountOverCutoff) {
                                            FileTransfer.actionList.size
                                        } else {
                                            FileTransfer.actionList.size - 1
                                        },
                                    )
                                    try {
                                        if (input.toInt() in availableActionRange) {
                                            userInput = input.toInt()
                                        } else {
                                            rejectInput()
                                        }
                                    } catch (e: NumberFormatException) {
                                        rejectInput()
                                    }
                                }
                            }
                            printLine()

                            when (userInput) {
                                FileTransfer.NORMAL,
                                FileTransfer.CANCEL,
                                FileTransfer.SKIP_INVALID,
                                FileTransfer.COMPRESS_EVERYTHING,
                                FileTransfer.COMPRESS_INVALID,
                                -> {
                                    child.sendInt(userInput)
                                    break
                                }

                                FileTransfer.SHOW_ALL -> continue
                                else -> throw IllegalStateException("kinda wack")
                            }
                        }
                    }

                    ServerFlags.SEND_FILES_COLLECTING -> {
                        var fileCount by liveVarOf(0)
                        section {
                            text(text = "Total files found: ")
                            text(text = fileCount.toString())
                        }.run {
                            while (true) {
                                when (child.receiveInt()) {
                                    ServerFlags.HAS_MORE -> {
                                        fileCount = child.receiveInt()
                                    }

                                    ServerFlags.DONE -> {
                                        break
                                    }

                                    else -> Unit
                                }
                            }
                        }
                    }

                    ServerFlags.DONE -> break
                }
            }
        }

        is SetDefaultPortCommand -> setDefaultPortExecution(command = command)
        is ServerPasswordCommand -> serverPasswordExecution(command = command)
        is ExitCommand -> SessionManager.sessionActive = false
        is SessionCommand -> return
        else -> throw IllegalStateException("No valid command detected.")
    }
}

private fun Session.renderCutoffPath(path: String, serverDestinationDirLength: Int, index: Int) {
    val cutoff = path.length - ((serverDestinationDirLength + path.length) - 127)
    section {
        text(text = "${index + 1} ")
        text(text = path.subSequence(0, cutoff).toString())
        red { textLine(text = path.substring(startIndex = cutoff)) }
    }.run()
}
