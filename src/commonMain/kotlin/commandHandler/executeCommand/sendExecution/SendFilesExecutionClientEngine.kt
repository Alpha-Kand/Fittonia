package commandHandler.executeCommand.sendExecution

import commandHandler.FileTransfer
import commandHandler.SendFilesCommand
import commandHandler.ServerFlags
import commandHandler.canContinue
import commandHandler.executeCommand.sendExecution.helpers.FileZipper
import commandHandler.executeCommand.sendExecution.helpers.SendFileItemInfo
import commandHandler.executeCommand.sendExecution.helpers.SourceFileListManager
import commandHandler.setupSendCommandClient
import hmeadowSocket.HMeadowSocketClient
import reportTextLine

fun sendFilesExecutionClientEngine(command: SendFilesCommand, parent: HMeadowSocketClient) {
    val client = setupSendCommandClient(command = command)
    client.sendInt(ServerFlags.SEND_FILES)
    if (canContinue(command = command, client = client, parent = parent)) {
        val serverDestinationDirLength = client.sendFilesClientSetup(job = command.getJob())

        parent.reportTextLine(text = "Finding files to send...\uD83D\uDD0E")
        parent.sendInt(message = ServerFlags.SEND_FILES_COLLECTING)
        val sourceFileListManager = SourceFileListManager(
            userInputPaths = command.getFiles(),
            serverDestinationDirLength = serverDestinationDirLength,
            onItemFound = parent::reportFindingFiles,
        )
        parent.sendInt(ServerFlags.DONE)

        val choice = if (sourceFileListManager.foundItemNameTooLong) {
            parent.sendInt(ServerFlags.FILE_NAMES_TOO_LONG)
            parent.sendInt(serverDestinationDirLength)
            sourceFileListManager.filesNameTooLong.forEach { fileName ->
                parent.sendInt(ServerFlags.HAS_MORE)
                parent.sendString(fileName)
            }
            parent.sendInt(ServerFlags.DONE)
            parent.receiveInt()
        } else {
            FileTransfer.NORMAL
        }

        when (choice) {
            FileTransfer.NORMAL -> {
                client.sendItemCount(itemCount = sourceFileListManager.totalItemCount)
                sourceFileListManager.forEachItem { fileInfo ->
                    client.sendItem(sendFileItemInfo = fileInfo)
                }
                parent.reportTextLine(text = "Done")
                parent.sendInt(ServerFlags.DONE)
            }

            FileTransfer.CANCEL -> {
                client.sendItemCount(itemCount = null)
                parent.sendInt(ServerFlags.DONE)
            }

            FileTransfer.SKIP_INVALID -> {
                val sendingFileAmount = sourceFileListManager.validItemCount
                parent.reportTextLine(text = "Sending $sendingFileAmount files.")
                client.sendItemCount(itemCount = sendingFileAmount)
                sourceFileListManager.forEachItem { fileInfo ->
                    if (!fileInfo.nameIsTooLong) {
                        client.sendItem(sendFileItemInfo = fileInfo)
                    }
                }
                parent.reportTextLine(text = "Done")
                parent.sendInt(ServerFlags.DONE)
            }

            FileTransfer.COMPRESS_EVERYTHING -> {
                client.sendItemCount(itemCount = 1)
                parent.reportTextLine(text = "Compressing files...")
                val fileZipper = FileZipper()
                sourceFileListManager.forEachItem { fileInfo ->
                    if (fileInfo.isFile) {
                        fileZipper.zipItem(fileInfo)
                    }
                }
                parent.reportTextLine(text = "Sending compressed file...")
                fileZipper.finalize { zipFilePath ->
                    client.sendString("compressed.zip")
                    client.sendBoolean(true) // compressed.zip is a file.
                    client.sendFile(filePath = zipFilePath)
                }
                parent.reportTextLine(text = "Done")
                parent.sendInt(ServerFlags.DONE)
            }

            FileTransfer.COMPRESS_INVALID -> {
                client.sendItemCount(itemCount = sourceFileListManager.validItemCount + 1)
                parent.reportTextLine(text = "Sending & compressing files...")
                val fileZipper = FileZipper()
                sourceFileListManager.forEachItem { fileInfo ->
                    if (fileInfo.nameIsTooLong) {
                        fileZipper.zipItem(fileInfo)
                    } else {
                        client.sendItem(sendFileItemInfo = fileInfo)
                    }
                }
                parent.reportTextLine(text = "Sending compressed file...")
                fileZipper.finalize { zipFilePath ->
                    client.sendString("compressed.zip")
                    client.sendBoolean(true) // compressed.zip is a file.
                    client.sendFile(filePath = zipFilePath)
                }
                parent.reportTextLine(text = "Done")
                parent.sendInt(ServerFlags.DONE)
            }

            else -> {
                println("send files else")
            }
        }
    }
    client.close()
}

fun HMeadowSocketClient.sendFilesClientSetup(job: String?): Int {
    job?.let { jobName ->
        sendInt(ServerFlags.HAVE_JOB_NAME)
        sendString(jobName)
    } ?: sendInt(ServerFlags.NEED_JOB_NAME)
    return receiveInt()
}

fun HMeadowSocketClient.sendItemCount(itemCount: Int?) = itemCount?.let {
    sendBoolean(true)
    sendInt(itemCount)
} ?: sendBoolean(false)


private fun HMeadowSocketClient.reportFindingFiles(amount: Int) {
    sendInt(message = ServerFlags.HAS_MORE)
    sendInt(message = amount)
}

internal fun HMeadowSocketClient.sendItem(sendFileItemInfo: SendFileItemInfo) {
    sendString(sendFileItemInfo.relativePath)
    sendBoolean(sendFileItemInfo.isFile)
    if (sendFileItemInfo.isFile) {
        sendFile(filePath = sendFileItemInfo.absolutePath)
    }
}
