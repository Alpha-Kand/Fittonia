package commandHandler.executeCommand.sendExecution

import KotterSession.kotter
import OutputIO.printlnIO
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.runUntilInputEntered
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.Color
import com.varabyte.kotter.foundation.text.text
import commandHandler.FileTransfer
import commandHandler.FileTransfer.Companion.toName
import commandHandler.SendFilesCommand
import commandHandler.ServerFlagsString
import commandHandler.canContinueSendCommand
import commandHandler.executeCommand.sendExecution.helpers.FileZipper
import commandHandler.executeCommand.sendExecution.helpers.SendFileItemInfo
import commandHandler.executeCommand.sendExecution.helpers.SourceFileListManager
import commandHandler.setupSendCommandClient
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.cannot_send_file_path_too_long
import fittonia.composeapp.generated.resources.send_files_user_option_cancel
import fittonia.composeapp.generated.resources.send_files_user_option_compress_all
import fittonia.composeapp.generated.resources.send_files_user_option_compress_invalid
import fittonia.composeapp.generated.resources.send_files_user_option_show_all
import fittonia.composeapp.generated.resources.send_files_user_option_skip_invalid
import hmeadowSocket.HMeadowSocketClient
import org.jetbrains.compose.resources.getString

suspend fun sendFilesExecution(command: SendFilesCommand) {
    val client = setupSendCommandClient(command = command)
    if (command.canContinueSendCommand(client = client)) {
        printlnIO("Sending files")
        val serverDestinationDirLength = client.sendFilesClientSetup2(job = command.getJob())
        printlnIO("Server destination dir length: $serverDestinationDirLength", color = Color.BLUE)
        val sourceFileListManager = sendFilesCollecting(
            command = command,
            serverDestinationDirLength = serverDestinationDirLength,
        )
        val choice = fileNamesTooLong(
            sourceFileListManager = sourceFileListManager,
            serverDestinationDirLength = serverDestinationDirLength,
        )
        client.sendString(choice)
        when (choice) {
            FileTransfer.NORMAL.toName -> {
                printlnIO(text = "Sending ${sourceFileListManager.totalItemCount} files.")
                client.sendFilesNormal(sourceFileListManager = sourceFileListManager)
                printlnIO(text = "Done")
            }

            FileTransfer.CANCEL.toName -> Unit

            FileTransfer.SKIP_INVALID.toName -> {
                printlnIO(text = "Sending ${sourceFileListManager.totalItemCount} files.")
                client.sendFilesSkipInvalid(sourceFileListManager = sourceFileListManager)
                printlnIO(text = "Done")
            }

            FileTransfer.COMPRESS_EVERYTHING.toName -> {
                printlnIO(text = "Compressing and sending files...")
                client.sendFilesCompressEverything(sourceFileListManager = sourceFileListManager)
                printlnIO(text = "Done")
                printlnIO(ServerFlagsString.DONE)
            }

            FileTransfer.COMPRESS_INVALID.toName -> {
                printlnIO(text = "Sending & compressing files...")
                client.sendFilesCompressInvalid(sourceFileListManager = sourceFileListManager)
                printlnIO(text = "Done")
                printlnIO(ServerFlagsString.DONE)
            }

            else -> {
                printlnIO("send files else")
            }
        }
    }
}

internal fun sendFilesCollecting(
    command: SendFilesCommand,
    serverDestinationDirLength: Int,
): SourceFileListManager {
    printlnIO("Finding files to send...\uD83D\uDD0E")
    var sourceFileListManager: SourceFileListManager? = null
    var fileCount by kotter.liveVarOf(0)
    kotter.section {
        text(text = "Total files found: ")
        text(text = fileCount.toString())
    }.run {
        sourceFileListManager = SourceFileListManager(
            userInputPaths = command.getFiles(),
            serverDestinationDirLength = serverDestinationDirLength,
            onItemFound = {
                fileCount = it
            },
        )
    }

    return sourceFileListManager!!
}

fun HMeadowSocketClient.sendFilesClientSetup2(job: String?): Int {
    job?.let { jobName ->
        sendString(ServerFlagsString.HAVE_JOB_NAME)
        sendString(jobName)
    } ?: sendString(ServerFlagsString.NEED_JOB_NAME)
    return receiveInt()
}

internal suspend fun fileNamesTooLong(
    serverDestinationDirLength: Int,
    sourceFileListManager: SourceFileListManager,
): String {
    if (!sourceFileListManager.foundItemNameTooLong) return FileTransfer.NORMAL.toName
    val pathCount = sourceFileListManager.filesNameTooLong.size

    printlnIO(
        resource = Res.string.cannot_send_file_path_too_long,
        color = Color.YELLOW,
        pathCount,
    )
    printlnIO()

    val previewLimit = 10
    val pathCountOverCutoff = pathCount > previewLimit

    var userInput = 0
    while (true) {
        val previewCutoff = pathCountOverCutoff && userInput != FileTransfer.SHOW_ALL
        if (previewCutoff) {
            sourceFileListManager.filesNameTooLong.subList(0, previewLimit)
        } else {
            sourceFileListManager.filesNameTooLong
        }.forEachIndexed { index, path ->
            renderCutoffPath(
                path = path,
                serverDestinationDirLength = serverDestinationDirLength,
                index = index,
            )
        }

        if (previewCutoff) {
            printlnIO("...")
            renderCutoffPath(
                path = sourceFileListManager.filesNameTooLong.last(),
                serverDestinationDirLength = serverDestinationDirLength,
                index = sourceFileListManager.filesNameTooLong.lastIndex,
            )
        }

        val actionList = if (pathCountOverCutoff) {
            FileTransfer.defaultActionList
        } else {
            FileTransfer.defaultActionList.filter { it != FileTransfer.SHOW_ALL }
        }
        val optionStringMap = mapOf(
            FileTransfer.CANCEL to getString(Res.string.send_files_user_option_cancel),
            FileTransfer.SKIP_INVALID to getString(Res.string.send_files_user_option_skip_invalid),
            FileTransfer.COMPRESS_EVERYTHING to getString(Res.string.send_files_user_option_compress_all),
            FileTransfer.COMPRESS_INVALID to getString(Res.string.send_files_user_option_compress_invalid),
            FileTransfer.SHOW_ALL to getString(Res.string.send_files_user_option_show_all),
        )

        kotter.section { fileNamesTooLongRenderBlock(actionList, optionStringMap) }.runUntilInputEntered {
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

        printlnIO()

        when (userInput) {
            FileTransfer.NORMAL,
            FileTransfer.CANCEL,
            FileTransfer.SKIP_INVALID,
            FileTransfer.COMPRESS_EVERYTHING,
            FileTransfer.COMPRESS_INVALID,
            -> return userInput.toName

            FileTransfer.SHOW_ALL -> continue
            else -> throw IllegalStateException("kinda wack") // TODO
        }
    }
}

private fun renderCutoffPath(path: String, serverDestinationDirLength: Int, index: Int) {
    val cutoff = path.length - ((serverDestinationDirLength + path.length) - 127)
    kotter.section {
        renderCutoffPath(index, path, cutoff)
    }.run()
}

internal fun HMeadowSocketClient.sendItem(sendFileItemInfo: SendFileItemInfo) {
    sendString(sendFileItemInfo.relativePath)
    sendBoolean(sendFileItemInfo.isFile)
    if (sendFileItemInfo.isFile) {
        sendFile(filePath = sendFileItemInfo.absolutePath)
    }
    receiveContinue()
}

internal fun HMeadowSocketClient.sendFilesNormal(sourceFileListManager: SourceFileListManager) {
    sendInt(sourceFileListManager.totalItemCount)
    sourceFileListManager.forEachItem { fileInfo ->
        sendItem(sendFileItemInfo = fileInfo)
    }
}

internal fun HMeadowSocketClient.sendFilesSkipInvalid(sourceFileListManager: SourceFileListManager) {
    sendInt(sourceFileListManager.validItemCount)
    sourceFileListManager.forEachItem { fileInfo ->
        if (!fileInfo.nameIsTooLong) {
            sendItem(sendFileItemInfo = fileInfo)
        }
    }
}

internal fun HMeadowSocketClient.sendFilesCompressEverything(sourceFileListManager: SourceFileListManager) {
    sendInt(1)
    val fileZipper = FileZipper()
    sourceFileListManager.forEachItem { fileInfo ->
        fileZipper.zipItem(fileInfo)
    }
    finalizeFileZipper(fileZipper = fileZipper)
}

internal fun HMeadowSocketClient.sendFilesCompressInvalid(sourceFileListManager: SourceFileListManager) {
    sendInt(sourceFileListManager.validItemCount + 1)
    val fileZipper = FileZipper()
    sourceFileListManager.forEachItem { fileInfo ->
        if (fileInfo.nameIsTooLong) {
            fileZipper.zipItem(fileInfo)
        } else {
            sendItem(sendFileItemInfo = fileInfo)
        }
    }
    finalizeFileZipper(fileZipper = fileZipper)
}

private fun HMeadowSocketClient.finalizeFileZipper(fileZipper: FileZipper) {
    fileZipper.finalize { zipFilePath ->
        sendString("compressed.zip")
        sendBoolean(true) // compressed.zip is a file.
        sendFile(filePath = zipFilePath)
        receiveContinue()
    }
}
