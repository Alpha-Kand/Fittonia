package commandHandler.executeCommand

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import commandHandler.DumpCommand
import settingsManager.SettingsManager
import java.nio.file.Files
import java.nio.file.Paths

fun Session.dumpExecution(command: DumpCommand) = section {
    val settingsManager = SettingsManager.settingsManager
    command.getDumpPath()?.let {
        val path = Paths.get(it).toAbsolutePath()
        if (Files.exists(path)) {
            if (Files.isDirectory(path)) {
                settingsManager.setDumpPath(path.toString())

                if (Files.list(path).findFirst().isPresent) {
                    textLine("Warning: New dump path is not empty.")
                }
            } else {
                textLine("Supplied path was not a valid directory.")
            }
        } else {
            textLine("Supplied path does not exist.")
        }
    } ?: run {
        if (settingsManager.settings.dumpPath.isEmpty()) {
            textLine("No dump path set.")
        } else {
            textLine("Current dump path: " + settingsManager.settings.dumpPath)
        }
    }
}.run()
