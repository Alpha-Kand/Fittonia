package commandHandler.executeCommand.sendExecution

import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.Color
import com.varabyte.kotter.foundation.text.color
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import commandHandler.FileTransfer
import commandHandler.ServerFlags
import hmeadowSocket.HMeadowSocketServer
import printLine

fun Session.sendFilesExecution(inputTokens: List<String>) {
    startClientEngine(inputTokens = inputTokens)

    val clientEngine = HMeadowSocketServer.getServer(port = 10778)
    while (true) {
        when (clientEngine.receiveInt()) {
            ServerFlags.PRINT_LINE -> clientEnginePrintLine(clientEngine = clientEngine)
            ServerFlags.FILE_NAMES_TOO_LONG -> clientEngineFileNamesTooLong(clientEngine = clientEngine)
            ServerFlags.SEND_FILES_COLLECTING -> clientEngineSendFilesCollecting(clientEngine = clientEngine)
            ServerFlags.DONE -> break
        }
    }
}

private fun Session.clientEnginePrintLine(clientEngine: HMeadowSocketServer) {
    section {
        color(Color.values()[clientEngine.receiveInt()])
        textLine(text = clientEngine.receiveString())
    }.run()
}

private fun Session.clientEngineSendFilesCollecting(clientEngine: HMeadowSocketServer) {
    var fileCount by liveVarOf(0)
    section {
        text(text = "Total files found: ")
        text(text = fileCount.toString())
    }.run {
        while (true) {
            when (clientEngine.receiveInt()) {
                ServerFlags.HAS_MORE -> {
                    fileCount = clientEngine.receiveInt()
                }

                ServerFlags.DONE -> {
                    break
                }

                else -> Unit
            }
        }
    }
}

private fun Session.clientEngineFileNamesTooLong(clientEngine: HMeadowSocketServer) {
    val serverDestinationDirLength = clientEngine.receiveInt()
    val filePaths = mutableListOf<String>()
    while (clientEngine.receiveInt() == ServerFlags.HAS_MORE) {
        filePaths.add(clientEngine.receiveString())
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

        val actionList =
            FileTransfer.defaultActionList + if (pathCountOverCutoff) listOf(FileTransfer.SHOW_ALL) else emptyList()
        section {
            textLine()
            val sb = StringBuilder("What would you like to do? (")
            actionList.forEach {
                sb.append("$it,")
            }
            sb.dropLast(1)
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
        }.runUntilInputEntered {
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
        }
        printLine()

        when (userInput) {
            FileTransfer.NORMAL,
            FileTransfer.CANCEL,
            FileTransfer.SKIP_INVALID,
            FileTransfer.COMPRESS_EVERYTHING,
            FileTransfer.COMPRESS_INVALID,
            -> {
                clientEngine.sendInt(userInput)
                break
            }

            FileTransfer.SHOW_ALL -> continue
            else -> throw IllegalStateException("kinda wack") // TODO
        }
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
