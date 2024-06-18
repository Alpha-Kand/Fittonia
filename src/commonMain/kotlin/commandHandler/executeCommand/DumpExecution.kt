package commandHandler.executeCommand

import OutputIO.printlnIO
import commandHandler.DumpCommand
import dumpPathCurrent
import dumpPathDoesntExist
import dumpPathNotEmptyWarning
import dumpPathNotSet
import dumpPathNotValidDirectory
import dumpPathNotWritable
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
                        warningIO(dumpPathNotEmptyWarning)
                    }
                } else {
                    errorIO(dumpPathNotWritable)
                }
            } else {
                errorIO(dumpPathNotValidDirectory)
            }
        } else {
            errorIO(dumpPathDoesntExist)
        }
    } ?: run {
        if (settingsManager.settings.dumpPath.isEmpty()) {
            printlnIO(text = dumpPathNotSet)
        } else {
            printlnIO(text = dumpPathCurrent.format(settingsManager.settings.dumpPath))
        }
    }
}
