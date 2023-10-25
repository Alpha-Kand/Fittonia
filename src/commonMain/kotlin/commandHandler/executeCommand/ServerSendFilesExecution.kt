package commandHandler.executeCommand

import com.varabyte.kotter.foundation.text.green
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import commandHandler.FileTransfer
import commandHandler.ServerFlags
import commandHandler.receivePassword
import commandHandler.sendConfirmation
import hmeadowSocket.HMeadowSocketServer
import settingsManager.SettingsManager
import java.nio.file.Files
import kotlin.io.path.Path

fun Session.serverSendFilesExecution(server: HMeadowSocketServer) = section {
    server.sendConfirmation()
    if (!server.receivePassword()) return@section

    val jobPath = determineJobPath(server = server)
    Files.createDirectory(Path(jobPath))

    val tempReceivingFolder = Files.createTempDirectory(FileTransfer.tempPrefix)
    val fileTransferCount = server.receiveInt()

    repeat(fileTransferCount) {
        val relativePath = server.receiveString()
        val receivedLocalDir = relativePath.substring(startIndex = 2)
        val destinationPath = "$jobPath/$receivedLocalDir"
        if (relativePath.substring(0, 2) == FileTransfer.filePrefix) {
            text("Receiving: $receivedLocalDir")
            val (tempFile, _) = server.receiveFile(
                destination = "$tempReceivingFolder/",
                prefix = FileTransfer.tempPrefix,
                suffix = FileTransfer.tempSuffix,
            )

            Files.move(Path(tempFile), Path(destinationPath))
            green { textLine(" Done.") }
        } else {
            Files.createDirectory(Path(destinationPath))
        }
    }
    textLine()
}.run()

private fun determineJobPath(server: HMeadowSocketServer): String {
    val settingsManager = SettingsManager.settingsManager
    val initialJobName = if (server.receiveInt() == ServerFlags.NEED_JOB_NAME) {
        settingsManager.getAutoJobName()
    } else {
        server.receiveString()
    }

    var nonConflictedJobName: String = initialJobName
    while (Files.exists(Path(path = settingsManager.settings.dumpPath + "/$nonConflictedJobName"))) {
        nonConflictedJobName = initialJobName + "_" + settingsManager.getAutoJobName()
    }
    return settingsManager.settings.dumpPath + "/$nonConflictedJobName"
}
