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
import printLine
import settingsManager.SettingsManager
import java.nio.file.Files
import kotlin.io.path.Path

fun Session.serverSendFilesExecution(server: HMeadowSocketServer) {
    server.sendConfirmation()
    if (!server.receivePassword()) return

    val jobPath = determineJobPath(server = server)
    Files.createDirectory(Path(jobPath))
    server.sendInt(jobPath.length)

    val tempReceivingFolder = Files.createTempDirectory(FileTransfer.tempPrefix)
    when (server.receiveInt()) {
        ServerFlags.CANCEL_SEND_FILES -> {
            printLine(text = "Client cancelled sending files.")
            printLine()
            return
        }

        else -> Unit
    }

    val fileTransferCount = server.receiveInt()
    printLine(text = "$fileTransferCount")

    repeat(times = fileTransferCount) {
        val relativePath = server.receiveString()
        val destinationPath = "$jobPath/$relativePath"
        if (server.receiveBoolean()) { // Is a file.
            section {
                text("Receiving: $relativePath")
                val (tempFile, _) = server.receiveFile(
                    destination = "$tempReceivingFolder/",
                    prefix = FileTransfer.tempPrefix,
                    suffix = FileTransfer.tempSuffix,
                )
                Files.move(Path(tempFile), Path(destinationPath))
                green { textLine(text = " Done.") }
            }.run()
        } else {
            Files.createDirectory(Path(destinationPath))
        }
    }
    printLine(text = "$fileTransferCount file(s) received")
    printLine()
}

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
