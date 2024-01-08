package commandHandler.executeCommand

import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.green
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import commandHandler.FileTransfer
import commandHandler.ServerFlags
import commandHandler.receivePassword
import commandHandler.sendConfirmation
import fileOperationWrappers.FileOperations
import hmeadowSocket.HMeadowSocketServer
import printLine
import settingsManager.SettingsManager
import java.nio.file.Path
import kotlin.io.path.Path

fun Session.serverSendFilesExecution(server: HMeadowSocketServer) {
    // ~~~~~~~~~~
    server.sendConfirmation()
    if (!server.receivePassword()) return
    // ~~~~~~~~~~

    val jobPath = server.sendFilesServerSetup()
    val (tempReceivingFolder, fileTransferCount, clientCancelled) = server.waitForItemCount()
    if (clientCancelled) {
        printLine(text = "Client cancelled sending files.")
        printLine()
        return
    }
    printLine(text = "$fileTransferCount")
    repeat(times = fileTransferCount) {
        var relativePath by liveVarOf(value = "")
        var complete by liveVarOf(value = false)
        section {
            text("Receiving: $relativePath")
            if (complete) green { textLine(text = " Done.") }
        }.run {
            server.receiveItem(
                jobPath = jobPath,
                tempReceivingFolder = tempReceivingFolder,
                onGetRelativePath = { relativePath = it },
                onDone = { complete = true },
            )
        }
    }
    printLine(text = "$fileTransferCount file(s) received")
    printLine()
}

fun HMeadowSocketServer.sendFilesServerSetup(): String {
    val jobPath = determineJobPath()
    FileOperations.createDirectory(path = Path(jobPath))
    sendInt(jobPath.length)
    return jobPath
}

private fun HMeadowSocketServer.determineJobPath(): String {
    val settingsManager = SettingsManager.settingsManager
    val initialJobName = if (receiveInt() == ServerFlags.NEED_JOB_NAME) {
        settingsManager.getAutoJobName()
    } else {
        receiveString()
    }

    var nonConflictedJobName: String = initialJobName
    while (FileOperations.exists(Path(path = settingsManager.settings.dumpPath + "/$nonConflictedJobName"))) {
        nonConflictedJobName = initialJobName + "_" + settingsManager.getAutoJobName()
    }
    return settingsManager.settings.dumpPath + "/$nonConflictedJobName"
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
    onDone()
}