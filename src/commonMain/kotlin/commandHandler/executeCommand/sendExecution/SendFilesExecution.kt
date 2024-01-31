package commandHandler.executeCommand.sendExecution

import KotterRunType
import KotterSession.kotter
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.Color
import com.varabyte.kotter.foundation.text.color
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import commandHandler.FileTransfer
import commandHandler.ServerFlagsString
import hmeadowSocket.HMeadowSocketServer
import kotterSection
import printLine

fun sendFilesExecution(inputTokens: List<String>) {
    val clientEngine = HMeadowSocketServer.createServerAnyPort(startingPort = 10778) { port ->
        startClientEngine(inputTokens = inputTokens + listOf("clientengineport=$port"))
    }
    while (true) {
        when (clientEngine.receiveString()) {
            ServerFlagsString.PRINT_LINE -> clientEngine.clientEnginePrintLine()
            ServerFlagsString.FILE_NAMES_TOO_LONG -> clientEngine.fileNamesTooLong()
            ServerFlagsString.SEND_FILES_COLLECTING -> clientEngine.sendFilesCollecting()
            ServerFlagsString.DONE -> break
        }
    }
}

private fun HMeadowSocketServer.clientEnginePrintLine() {
    val colourIndex = receiveInt()
    val message = receiveString()
    sendContinue()
    kotterSection(
        renderBlock = {
            color(Color.entries[colourIndex])
            textLine(text = message)
        },
    )
}

private fun HMeadowSocketServer.sendFilesCollecting() {
    var fileCount by kotter.liveVarOf(0)
    kotterSection(
        renderBlock = {
            text(text = "Total files found: ")
            text(text = fileCount.toString())
        },
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
        }
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
            renderBlock = {
                textLine()
                val sb = StringBuilder("What would you like to do? (")
                actionList.forEach {
                    sb.append("$it,")
                }
                sb.dropLast(2)
                sb.append(')')
                textLine(text = sb.toString())

                actionList.forEach { action ->
                    when (action) {
                        FileTransfer.CANCEL -> textLine(text = "$action. Cancel sending files.")
                        FileTransfer.SKIP_INVALID -> textLine(text = "$action. Skip invalid files.")
                        FileTransfer.COMPRESS_EVERYTHING -> textLine(text = "$action. Compress all files and send as a single file.")
                        FileTransfer.COMPRESS_INVALID -> textLine(text = "$action. Compress invalid files only and send as a single file (relative file paths will be preserved).")
                        FileTransfer.SHOW_ALL -> textLine(text = "$action. Show all files and ask again.")
                    }
                }
                text(text = "> "); input()
            },
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

private fun renderCutoffPath(path: String, serverDestinationDirLength: Int, index: Int) {
    val cutoff = path.length - ((serverDestinationDirLength + path.length) - 127)
    kotterSection(
        renderBlock = {
            text(text = "${index + 1} ")
            text(text = path.subSequence(0, cutoff).toString())
            red { textLine(text = path.substring(startIndex = cutoff)) }
        },
    )
}
