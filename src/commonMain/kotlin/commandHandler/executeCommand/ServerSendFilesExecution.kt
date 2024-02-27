package commandHandler.executeCommand

import commandHandler.FileTransfer
import commandHandler.ServerFlagsString
import commandHandler.ServerFlagsString.Companion.SHARE_JOB_NAME
import fileOperationWrappers.FileOperations
import hmeadowSocket.HMeadowSocketClient
import hmeadowSocket.HMeadowSocketServer
import java.nio.file.Path
import kotlin.io.path.Path

fun HMeadowSocketServer.serverSendFilesExecution(serverParent: HMeadowSocketClient) {
    val jobPath = sendFilesServerSetup(serverParent = serverParent)
    serverParent.sendString(SHARE_JOB_NAME)
    serverParent.sendString(jobPath)
    val (tempReceivingFolder, fileTransferCount, clientCancelled) = waitForItemCount()
    if (clientCancelled) {
        println("Client cancelled sending files.")
        println()
        return
    }
    repeat(times = fileTransferCount) {
        receiveItemAndReport(jobPath = jobPath, tempReceivingFolder = tempReceivingFolder, serverParent = serverParent)
    }
    println("${jobPath.split('/').last()}: $fileTransferCount file(s) received")
    println()
}

fun HMeadowSocketServer.receiveItemAndReport(
    jobPath: String,
    tempReceivingFolder: Path,
    serverParent: HMeadowSocketClient
) {
    serverParent.sendString(ServerFlagsString.RECEIVING_ITEM)
    receiveItem(
        jobPath = jobPath,
        tempReceivingFolder = tempReceivingFolder,
        onGetRelativePath = { relativePath ->
            serverParent.sendString(relativePath)
        },
        onDone = { serverParent.sendContinue() },
    )
}

fun HMeadowSocketServer.sendFilesServerSetup(serverParent: HMeadowSocketClient): String {
    val jobPath = determineJobPath(serverParent = serverParent)
    FileOperations.createDirectory(path = Path(jobPath))
    sendInt(jobPath.length)
    return jobPath
}

private fun HMeadowSocketServer.determineJobPath(serverParent: HMeadowSocketClient): String {
    return when (receiveString()) {
        ServerFlagsString.NEED_JOB_NAME -> {
            serverParent.sendString(ServerFlagsString.NEED_JOB_NAME)
            serverParent.receiveString()
        }

        ServerFlagsString.HAVE_JOB_NAME -> {
            val clientJobName = receiveString()
            serverParent.sendString(ServerFlagsString.HAVE_JOB_NAME)
            serverParent.sendString(clientJobName)
            serverParent.receiveString()
        }

        else -> throw Exception() //TODO
    }
}

fun HMeadowSocketServer.waitForItemCount() = if (receiveBoolean()) {
    Triple(FileOperations.createTempDirectory(FileTransfer.tempPrefix), receiveInt(), false)
} else {
    Triple(Path(""), -1, true)
}

fun HMeadowSocketServer.receiveItem(
    jobPath: String,
    tempReceivingFolder: Path,
    onGetRelativePath: (String) -> Unit,
    onDone: () -> Unit,
) {
    val relativePath = receiveString()
    onGetRelativePath(relativePath)
    val destinationPath = "$jobPath/$relativePath"
    if (receiveBoolean()) { // Is a file.
        val (tempFile, _) = receiveFile(
            destination = "$tempReceivingFolder/",
            prefix = FileTransfer.tempPrefix,
            suffix = FileTransfer.tempSuffix,
        )
        FileOperations.move(source = Path(tempFile), destination = Path(destinationPath))
    } else {
        FileOperations.createDirectory(path = Path(destinationPath))
    }
    sendContinue()
    onDone()
}