package commandHandler.executeCommand.sendExecution

import commandHandler.FileTransfer
import commandHandler.SendFilesCommand
import commandHandler.ServerFlagsString
import commandHandler.canContinueSendCommand
import commandHandler.executeCommand.sendExecution.helpers.FileZipper
import commandHandler.executeCommand.sendExecution.helpers.SendFileItemInfo
import commandHandler.executeCommand.sendExecution.helpers.SourceFileListManager
import commandHandler.setupSendCommandClient
import hmeadowSocket.HMeadowSocketClient
import reportTextLine

fun sendFilesExecutionClientEngine(command: SendFilesCommand, parent: HMeadowSocketClient) {
    val client = setupSendCommandClient(command = command)
    if (canContinueSendCommand(command = command, client = client, parent = parent)) {
        val serverDestinationDirLength = client.sendFilesClientSetup(job = command.getJob())
        val sourceFileListManager = parent.sendFilesCollecting(
            command = command,
            serverDestinationDirLength = serverDestinationDirLength,
        )
        val choice = parent.foundFileNamesTooLong(
            sourceFileListManager = sourceFileListManager,
            serverDestinationDirLength = serverDestinationDirLength,
        )

        when (choice) {
            FileTransfer.NORMAL -> {
                parent.reportTextLine(text = "Sending ${sourceFileListManager.totalItemCount} files.")
                client.sendFilesNormal(sourceFileListManager = sourceFileListManager)
                parent.reportTextLine(text = "Done")
                parent.sendString(ServerFlagsString.DONE)
            }

            FileTransfer.CANCEL -> {
                client.sendItemCount(itemCount = null)
                parent.sendString(ServerFlagsString.DONE)
            }

            FileTransfer.SKIP_INVALID -> {
                parent.reportTextLine(text = "Sending ${sourceFileListManager.totalItemCount} files.")
                client.sendFilesSkipInvalid(sourceFileListManager = sourceFileListManager)
                parent.reportTextLine(text = "Done")
                parent.sendString(ServerFlagsString.DONE)
            }

            FileTransfer.COMPRESS_EVERYTHING -> {
                parent.reportTextLine(text = "Compressing and sending files...")
                client.sendFilesCompressEverything(sourceFileListManager = sourceFileListManager)
                parent.reportTextLine(text = "Done")
                parent.sendString(ServerFlagsString.DONE)
            }

            FileTransfer.COMPRESS_INVALID -> {
                parent.reportTextLine(text = "Sending & compressing files...")
                client.sendFilesCompressInvalid(sourceFileListManager = sourceFileListManager)
                parent.reportTextLine(text = "Done")
                parent.sendString(ServerFlagsString.DONE)
            }

            else -> {
                println("send files else")
            }
        }
    }
    client.close()
}

internal fun HMeadowSocketClient.sendFilesCollecting(
    command: SendFilesCommand,
    serverDestinationDirLength: Int,
): SourceFileListManager {
    reportTextLine(text = "Finding files to send...\uD83D\uDD0E")
    sendString(message = ServerFlagsString.SEND_FILES_COLLECTING)
    val sourceFileListManager = SourceFileListManager(
        userInputPaths = command.getFiles(),
        serverDestinationDirLength = serverDestinationDirLength,
        onItemFound = ::reportFindingFiles,
    )
    sendString(ServerFlagsString.DONE)
    return sourceFileListManager
}

internal fun HMeadowSocketClient.foundFileNamesTooLong(
    sourceFileListManager: SourceFileListManager,
    serverDestinationDirLength: Int,
) = if (sourceFileListManager.foundItemNameTooLong) {
    sendString(message = ServerFlagsString.FILE_NAMES_TOO_LONG)
    sendInt(serverDestinationDirLength)
    sourceFileListManager.filesNameTooLong.forEach { fileName ->
        sendString(ServerFlagsString.HAS_MORE)
        sendString(fileName)
    }
    sendString(ServerFlagsString.DONE)
    receiveInt()
} else {
    FileTransfer.NORMAL
}

fun HMeadowSocketClient.sendFilesClientSetup(job: String?): Int {
    job?.let { jobName ->
        sendString(ServerFlagsString.HAVE_JOB_NAME)
        sendString(jobName)
    } ?: sendString(ServerFlagsString.NEED_JOB_NAME)
    return receiveInt()
}

fun HMeadowSocketClient.sendItemCount(itemCount: Int?) = itemCount?.let {
    sendBoolean(true)
    sendInt(itemCount)
} ?: sendBoolean(false)

private fun HMeadowSocketClient.reportFindingFiles(amount: Int) {
    sendString(message = ServerFlagsString.HAS_MORE)
    sendInt(message = amount)
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
    sendItemCount(itemCount = sourceFileListManager.totalItemCount)
    sourceFileListManager.forEachItem { fileInfo ->
        sendItem(sendFileItemInfo = fileInfo)
    }
}

internal fun HMeadowSocketClient.sendFilesSkipInvalid(sourceFileListManager: SourceFileListManager) {
    sendItemCount(itemCount = sourceFileListManager.validItemCount)
    sourceFileListManager.forEachItem { fileInfo ->
        if (!fileInfo.nameIsTooLong) {
            sendItem(sendFileItemInfo = fileInfo)
        }
    }
}

internal fun HMeadowSocketClient.sendFilesCompressEverything(sourceFileListManager: SourceFileListManager) {
    sendItemCount(itemCount = 1)
    val fileZipper = FileZipper()
    sourceFileListManager.forEachItem { fileInfo ->
        fileZipper.zipItem(fileInfo)
    }
    finalizeFileZipper(fileZipper = fileZipper)
}

internal fun HMeadowSocketClient.sendFilesCompressInvalid(sourceFileListManager: SourceFileListManager) {
    sendItemCount(itemCount = sourceFileListManager.validItemCount + 1)
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
