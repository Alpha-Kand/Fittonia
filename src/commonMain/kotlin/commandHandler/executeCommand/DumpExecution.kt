package commandHandler.executeCommand

import OutputIO.printlnIO
import commandHandler.DumpCommand
import errorIO
import settingsManager.SettingsManager
import successIO
import warningIO
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

fun dumpExecution(command: DumpCommand) {
    val settingsManager = SettingsManager.settingsManager
    command.getDumpPath()?.let { path ->
        val expandedPath = if (path.startsWith("~" + File.separator)) {
            System.getProperty("user.home") + File.separator + path.substring(2)
        } else {
            path
        }
        val dumpPath = Paths.get(expandedPath).toAbsolutePath()
        if (Files.exists(dumpPath)) {
            if (Files.isDirectory(dumpPath)) {
                if (Files.isWritable(dumpPath)) {
                    settingsManager.setDumpPath(dumpPath.toString())
                    if (Files.list(dumpPath).findFirst().isPresent) {
                        successIO()
                        warningIO("New dump path is not empty.")
                    }
                } else {
                    errorIO("That directory is not writable. Check its permissions.")
                }
            } else {
                errorIO("Supplied path was not a valid directory.")
            }
        } else {
            errorIO("Supplied path does not exist.")
        }
    } ?: run {
        if (settingsManager.settings.dumpPath.isEmpty()) {
            printlnIO(output = "No dump path set.")
        } else {
            printlnIO(output = "Current dump path: " + settingsManager.settings.dumpPath)
        }
    }
}
