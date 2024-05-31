package commandHandler.executeCommand.sendExecution

import FittoniaError
import KotterRunType
import KotterSession.kotter
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.Color
import commandHandler.AddCommand
import commandHandler.FileTransfer
import commandHandler.SendCommand
import commandHandler.ServerFlagsString
import commandHandler.clientEnginePortArguments
import hmeadowSocket.HMeadowSocketServer
import kotterSection
import printLine
import settingsManager.SettingsManager

fun sendCommandExecution(command: SendCommand, inputTokens: List<String>) {
    // Create connection to client engine.
    val clientEngine = HMeadowSocketServer.createServerAnyPort(startingPort = 10778) { port ->
        // Create client engine.
        startClientEngine(inputTokens = inputTokens + listOf("${clientEnginePortArguments.first()}=$port"))
    }
    while (true) {
        when (clientEngine.receiveString()) {
            ServerFlagsString.PRINT_LINE -> clientEngine.clientEnginePrintLine()
            ServerFlagsString.FILE_NAMES_TOO_LONG -> clientEngine.fileNamesTooLong()
            ServerFlagsString.SEND_FILES_COLLECTING -> clientEngine.sendFilesCollecting()
            ServerFlagsString.ADD_DESTINATION -> clientEngine.addDestination(command = command)
            ServerFlagsString.DONE -> break
        }
    }
}

private fun HMeadowSocketServer.clientEnginePrintLine() {
    val colourIndex = receiveInt()
    val message = receiveString()
    sendContinue()
    kotterSection(renderBlock = { clientEnginePrintLineRenderBlock(colourIndex, message) })
}

private fun HMeadowSocketServer.sendFilesCollecting() {
    var fileCount by kotter.liveVarOf(0)
    kotterSection(
        renderBlock = { sendFilesCollectingRenderBlock(fileCount) },
        runBlock = {
            while (true) {
                when (receiveString()) {
                    ServerFlagsString.HAS_MORE -> {
                        fileCount = receiveInt()
                    }

                    ServerFlagsString.DONE -> {
                        break
                    }

                    else -> Unit
                }
            }
        },
    )
}

private fun HMeadowSocketServer.fileNamesTooLong() {
    val serverDestinationDirLength = receiveInt()
    val filePaths = mutableListOf<String>()
    while (receiveString() == ServerFlagsString.HAS_MORE) {
        filePaths.add(receiveString())
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

        val actionList = if (pathCountOverCutoff) {
            FileTransfer.defaultActionList
        } else {
            FileTransfer.defaultActionList.filter { it != FileTransfer.SHOW_ALL }
        }
        kotterSection(
            renderBlock = { fileNamesTooLongRenderBlock(actionList) },
            runBlock = {
                onInputEntered {
                    val availableActionRange = IntRange(
                        start = 1,
                        endInclusive = actionList.size,
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
            },
            runType = KotterRunType.RUN_UNTIL_INPUT_ENTERED,
        )
        printLine()

        when (userInput) {
            FileTransfer.NORMAL,
            FileTransfer.CANCEL,
            FileTransfer.SKIP_INVALID,
            FileTransfer.COMPRESS_EVERYTHING,
            FileTransfer.COMPRESS_INVALID,
            -> {
                sendInt(userInput)
                break
            }

            FileTransfer.SHOW_ALL -> continue
            else -> throw IllegalStateException("kinda wack") // TODO
        }
    }
}

private fun HMeadowSocketServer.addDestination(command: SendCommand) {
    if (command is AddCommand) {
        try {
            SettingsManager.settingsManager.addDestination(
                name = command.getName(),
                ip = command.getIP(),
                password = command.getPassword(),
            )
        } catch (e: FittoniaError) {
            sendBoolean(false)
            throw e
        }
        sendBoolean(true)
    } else {
        sendBoolean(false)
    }
}

private fun renderCutoffPath(path: String, serverDestinationDirLength: Int, index: Int) {
    val cutoff = path.length - ((serverDestinationDirLength + path.length) - 127)
    kotterSection(renderBlock = { renderCutoffPath(index, path, cutoff) })
}
